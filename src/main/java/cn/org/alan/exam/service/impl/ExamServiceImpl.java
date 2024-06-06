package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.exception.AppException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.ExamConverter;
import cn.org.alan.exam.converter.ExamQuAnswerConverter;
import cn.org.alan.exam.mapper.*;
import cn.org.alan.exam.model.entity.*;
import cn.org.alan.exam.model.form.exam.ExamAddForm;
import cn.org.alan.exam.model.form.exam.ExamUpdateForm;
import cn.org.alan.exam.model.form.examquanswer.ExamQuAnswerAddForm;
import cn.org.alan.exam.model.vo.exam.*;
import cn.org.alan.exam.model.vo.record.ExamRecordDetailVO;
import cn.org.alan.exam.service.IExamQuAnswerService;
import cn.org.alan.exam.service.IExamService;
import cn.org.alan.exam.service.IOptionService;
import cn.org.alan.exam.service.IQuestionService;
import cn.org.alan.exam.util.ClassTokenGenerator;
import cn.org.alan.exam.util.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考试服务实现类
 *
 * @author Alan
 * @since 2024-03-21
 */
@Service
public class ExamServiceImpl extends ServiceImpl<ExamMapper, Exam> implements IExamService {

    @Resource
    private ExamMapper examMapper;
    @Resource
    private ExamConverter examConverter;
    @Resource
    private IExamQuAnswerService examQuAnswerService;
    @Resource
    private QuestionMapper questionMapper;
    @Resource
    private IQuestionService questionService;
    @Resource
    private ExamGradeMapper examGradeMapper;
    @Resource
    private ExamRepoMapper examRepoMapper;
    @Resource
    private ExamQuestionMapper examQuestionMapper;
    @Resource
    private OptionMapper optionMapper;
    @Resource
    private IOptionService optionService;
    @Resource
    private ExamQuAnswerMapper examQuAnswerMapper;
    @Resource
    private UserExamsScoreMapper userExamsScoreMapper;
    @Resource
    private UserBookMapper userBookMapper;
    @Resource
    private ExamQuAnswerConverter examQuAnswerConverter;
    @Resource
    private UserMapper userMapper;
    @Resource
    private CertificateUserMapper certificateUserMapper;

    @Override
    @Transactional
    public Result<String> createExam(ExamAddForm examAddForm) {
        // 将关于考试相关的实体转换为Exam
        Exam exam = examConverter.formToEntity(examAddForm);
        // 添加考试信息到考试表
        int grossScore = examAddForm.getRadioCount() * examAddForm.getRadioScore()
                + examAddForm.getMultiCount() * examAddForm.getMultiScore()
                + examAddForm.getJudgeCount() * examAddForm.getJudgeScore()
                + examAddForm.getSaqCount() * examAddForm.getSaqScore();
        exam.setGrossScore(grossScore);
        int examRows = examMapper.insert(exam);
        if (examRows == 0) {
            return Result.failed("创建失败");
        }
        // 添加考试班级
        String gradeIdsStr = examAddForm.getGradeIds();
        List<Integer> gradeIds = Arrays.stream(gradeIdsStr.split(","))
                .map(Integer::parseInt)
                .toList();
        Integer gradeRows = examGradeMapper.addExamGrade(exam.getId(), gradeIds);
        if (gradeRows == 0) {
            return Result.failed("创建失败");
        }
        // 设置考试的题库
        Integer repoId = examAddForm.getRepoId();
        ExamRepo examRepo = new ExamRepo();
        examRepo.setExamId(exam.getId());
        examRepo.setRepoId(repoId);
        int examRepoRows = examRepoMapper.insert(examRepo);
        if (examRepoRows == 0) {
            return Result.failed("创建失败");
        }
        // 开始抽题
        // <"试题类型"，"题目数量">
        Map<Integer, Integer> quTypeToCount = new HashMap<>();
        quTypeToCount.put(1, exam.getRadioCount());
        quTypeToCount.put(2, exam.getMultiCount());
        quTypeToCount.put(3, exam.getJudgeCount());
        quTypeToCount.put(4, exam.getSaqCount());
        // <"试题类型"，"试题分数">
        Map<Integer, Integer> quTypeToScore = new HashMap<>();
        quTypeToScore.put(1, exam.getRadioScore());
        quTypeToScore.put(2, exam.getMultiScore());
        quTypeToScore.put(3, exam.getJudgeScore());
        quTypeToScore.put(4, exam.getSaqScore());
        int sortCounter = 0;
        for (Map.Entry<Integer, Integer> entry : quTypeToCount.entrySet()) {
            Map<Integer, Integer> questionSortMap = new HashMap<>();
            // 获取当前试题类型、试题数量、考试id、试题分数
            Integer quType = entry.getKey();
            Integer count = entry.getValue();
            Integer examId = exam.getId();
            Integer quScore = quTypeToScore.get(quType);
            // 查询设置题库中，对应类型的试题id
            LambdaQueryWrapper<Question> typeQueryWrapper = new LambdaQueryWrapper<>();
            typeQueryWrapper.select(Question::getId)
                    .eq(Question::getQuType, quType)
                    .eq(Question::getIsDeleted,0)
                    .eq(Question::getRepoId, examAddForm.getRepoId());
            List<Question> questionsByType = questionMapper.selectList(typeQueryWrapper);
            if (questionsByType.size() < count) {
                throw new AppException("题库中类型为" + quType + "的题目数量不足" + count + "个！");
            }
            List<Integer> typeQuestionIds = questionsByType.stream().map(Question::getId).collect(Collectors.toList());
            Collections.shuffle(typeQuestionIds);
            List<Integer> sampledIds = typeQuestionIds.subList(0, count);
            // 插入试题
            if (!sampledIds.isEmpty()) {
                for (Integer qId : sampledIds) {
                    questionSortMap.put(qId, sortCounter); // 为每个问题ID分配sort值
                    sortCounter++; // 每插入一题，sort计数器增加
                }
                // 准备数据结构以符合Mapper方法的输入要求
                List<Map<String, Object>> questionDetails = new ArrayList<>();
                for (Map.Entry<Integer, Integer> sortEntry : questionSortMap.entrySet()) {
                    Map<String, Object> detail = new HashMap<>();
                    detail.put("questionId", sortEntry.getKey());
                    detail.put("sort", sortEntry.getValue());
                    questionDetails.add(detail);
                }
                // 调整Mapper方法以接受新的参数结构
                int examQueRows = examQuestionMapper.insertQuestion(examId, quType, quScore, questionDetails);
                if (examQueRows == 0) {
                    return Result.failed("创建失败");
                }
            }

        }
        return Result.success("创建成功");
    }

    @Override
    @Transactional
    public Result<String> updateExam(ExamUpdateForm examUpdateForm, Integer examId) {
        // 修改考试表记录
        Exam examTemp = this.getById(examId);
        int grossScore = examTemp.getRadioCount() * examUpdateForm.getRadioScore()
                + examTemp.getMultiCount() * examUpdateForm.getMultiScore()
                + examTemp.getJudgeCount() * examUpdateForm.getJudgeScore()
                + examTemp.getSaqCount() * examUpdateForm.getSaqScore();
        LambdaUpdateWrapper<Exam> examLambdaUpdate = new LambdaUpdateWrapper<>();
        examLambdaUpdate.eq(Exam::getId, examId)
                .eq(Exam::getUserId, SecurityUtil.getUserId());
        Exam exam = examConverter.formToEntity(examUpdateForm);
        exam.setGrossScore(grossScore);
        int examRows = examMapper.update(exam, examLambdaUpdate);
        if (examRows == 0) {
            throw new AppException("修改失败");
        }
        return Result.success("修改成功");
    }

    @Override
    @Transactional
    public Result<String> deleteExam(String ids) {
        // 删除考试表记录
        List<Integer> examIds = Arrays.stream(ids.split(","))
                .map(Integer::parseInt)
                .toList();
        int examRows = examMapper.deleteExams(examIds);
        if (examRows == 0) {
            throw new AppException("删除失败，删除考试表时失败");
        }
        // // 删除考试班级记录
        // int examGradeRows = examMapper.deleteExamGrades(examIds);
        // if (examGradeRows == 0) {
        //     throw new AppException("删除失败，删除考试班级表时失败");
        // }
        // // 删除考试题库记录
        // int examRepoRows = examMapper.deleteExamRepos(examIds);
        // if (examRepoRows == 0) {
        //     throw new AppException("删除失败，删除考试题库表时失败");
        // }
        // // 删除考试试题记录
        // int examQueRows = examMapper.deleteExamQuestions(examIds);
        // if (examQueRows == 0) {
        //     throw new AppException("删除失败，删除考试试题表时失败");
        // }
        return Result.success("删除成功");
    }

    @Override
    public Result<IPage<ExamVO>> getPagingExam(Integer pageNum, Integer pageSize, String title) {
        // 创建Page对象
        Page<Exam> page = new Page<>(pageNum, pageSize);
        // 开始查询
        LambdaQueryWrapper<Exam> examQuery = new LambdaQueryWrapper<>();
        examQuery.like(StringUtils.isNotBlank(title), Exam::getTitle, title)
                .eq(Exam::getIsDeleted,0)
                .eq(Exam::getUserId, SecurityUtil.getUserId());
        Page<Exam> examPage = examMapper.selectPage(page, examQuery);
        // 实体转换
        Page<ExamVO> examVOPage = examConverter.pageEntityToVo(examPage);
        return Result.success("查询成功", examVOPage);
    }

    @Override
    public Result<ExamQuestionListVO> getQuestionList(Integer examId) {
        // 检查是否正在考试
        if (!isUserTakingExam(examId)) {
            return Result.failed("考试在进行");
        }
        ExamQuestionListVO examQuestionListVO = new ExamQuestionListVO();
        // 设置倒计时
        Exam byId = this.getById(examId);
        examQuestionListVO.setExamDuration(byId.getExamDuration());
        LambdaQueryWrapper<UserExamsScore> userExamsScoreLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userExamsScoreLambdaQueryWrapper.eq(UserExamsScore::getUserId, SecurityUtil.getUserId())
                .eq(UserExamsScore::getExamId, examId)
                .eq(UserExamsScore::getState, 0);
        UserExamsScore userExamsScore = userExamsScoreMapper.selectOne(userExamsScoreLambdaQueryWrapper);
        Calendar cl = Calendar.getInstance();
        LocalDateTime createTime = userExamsScore.getCreateTime();
        if (createTime != null) {
            Date date = Date.from(createTime.atZone(ZoneId.systemDefault()).toInstant());
            cl.setTime(date);
        } else {
            return Result.failed("错误");
        }
        cl.add(Calendar.MINUTE, byId.getExamDuration());
        examQuestionListVO.setLeftSeconds((cl.getTimeInMillis() - System.currentTimeMillis()) / 1000);
        // 添加不同类型的试题列表
        for (int i = 1; i <= 4; i++) {
            // 根据考试id查询考试试题表
            LambdaQueryWrapper<ExamQuestion> examQuestionLambdaQueryWrapper = new LambdaQueryWrapper<>();
            examQuestionLambdaQueryWrapper.eq(ExamQuestion::getExamId, examId)
                    .eq(ExamQuestion::getType, i)
                    .orderByAsc(ExamQuestion::getSort);
            List<ExamQuestion> examQuestionList = examQuestionMapper.selectList(examQuestionLambdaQueryWrapper);
            List<ExamQuestionVO> examQuestionVOS = examConverter.examQuestionListEntityToVO(examQuestionList);
            for (ExamQuestionVO temp : examQuestionVOS) {
                LambdaQueryWrapper<ExamQuAnswer> examQuAnswerLambdaQueryWrapper = new LambdaQueryWrapper<>();
                examQuAnswerLambdaQueryWrapper.eq(ExamQuAnswer::getQuestionId, temp.getQuestionId())
                        .eq(ExamQuAnswer::getExamId, examId)
                        .eq(ExamQuAnswer::getUserId, SecurityUtil.getUserId());
                List<ExamQuAnswer> examQuAnswers = examQuAnswerMapper.selectList(examQuAnswerLambdaQueryWrapper);
                if (examQuAnswers.size() > 0) {
                    temp.setCheckout(true);
                } else {
                    temp.setCheckout(false);
                }
            }
            if (examQuestionVOS.isEmpty()) {
                continue;
            }
            switch (i) {
                case 1 -> {
                    examQuestionListVO.setRadioList(examQuestionVOS);
                }
                case 2 -> {
                    examQuestionListVO.setMultiList(examQuestionVOS);
                }
                case 3 -> {
                    examQuestionListVO.setJudgeList(examQuestionVOS);
                }
                case 4 -> {
                    examQuestionListVO.setSaqList(examQuestionVOS);
                }
                default -> {

                }
            }

        }
        return Result.success("查询成功", examQuestionListVO);
    }

    @Override
    public Result<ExamQuDetailVO> getQuestionSingle(Integer examId, Integer questionId) {
        // 检查是否正在考试
        if (!isUserTakingExam(examId)) {
            return Result.failed("没有考试在进行");
        }
        ExamQuDetailVO examQuDetailVO = new ExamQuDetailVO();
        LambdaQueryWrapper<ExamQuestion> examQuestionLambdaQueryWrapper = new LambdaQueryWrapper<>();
        examQuestionLambdaQueryWrapper.eq(ExamQuestion::getQuestionId, questionId)
                .eq(ExamQuestion::getExamId, examId);
        ExamQuestion examQuestion = examQuestionMapper.selectOne(examQuestionLambdaQueryWrapper);
        examQuDetailVO.setSort(examQuestion.getSort());
        // 问题
        Question quById = questionService.getById(questionId);
        // 基本信息
        examQuDetailVO.setImage(quById.getImage());
        examQuDetailVO.setContent(quById.getContent());
        examQuDetailVO.setQuType(quById.getQuType());
        // 答案列表
        LambdaQueryWrapper<Option> optionLambdaQuery = new LambdaQueryWrapper<>();
        optionLambdaQuery.eq(Option::getQuId, questionId);
        List<Option> list = optionMapper.selectList(optionLambdaQuery);
        List<OptionVO> optionVOS = examConverter.opListEntityToVO(list);
        for (OptionVO temp : optionVOS) {

            LambdaQueryWrapper<ExamQuAnswer> examQuAnswerLambdaQueryWrapper = new LambdaQueryWrapper<>();
            examQuAnswerLambdaQueryWrapper.eq(ExamQuAnswer::getQuestionId, temp.getQuId())
                    .eq(ExamQuAnswer::getExamId, examId)
                    .eq(ExamQuAnswer::getUserId, SecurityUtil.getUserId());
            List<ExamQuAnswer> examQuAnswers = examQuAnswerMapper.selectList(examQuAnswerLambdaQueryWrapper);

            if (examQuAnswers.size() > 0) {
                for (ExamQuAnswer temp1 : examQuAnswers) {
                    Integer questionType = temp1.getQuestionType();
                    String answerId = temp1.getAnswerId();
                    String answerContent = temp1.getAnswerContent();
                    String idstr = temp.getId().toString();
                    switch (questionType) {
                        case 1, 3 -> {
                            if (answerId.equals(idstr)) {
                                temp.setCheckout(true);
                            } else {
                                temp.setCheckout(false);
                            }
                        }
                        case 2 -> {
                            // 解析用户作答
                            List<Integer> quIds = Arrays.stream(temp1.getAnswerId().split(","))
                                    .map(Integer::parseInt)
                                    .toList();
                            if (quIds.contains(temp.getId())) {
                                temp.setCheckout(true);
                            } else {
                                temp.setCheckout(false);
                            }

                        }
                        case 4 -> {
                            temp.setContent(answerContent);
                            examQuDetailVO.setAnswerList(optionVOS);
                        }
                        default -> {

                        }
                    }
                }
                ;
            }

        }
        if (quById.getQuType() != 4) {
            examQuDetailVO.setAnswerList(optionVOS);
        }

        return Result.success("获取成功", examQuDetailVO);
    }

    @Override
    public Result<List<ExamQuCollectVO>> getCollect(Integer examId) {
        // 检查是否正在考试
        if (!isUserTakingExam(examId)) {
            return Result.failed("没有考试在进行");
        }

        List<ExamQuCollectVO> examQuCollectVOS = new ArrayList<>();
        // 查询该考试的试题
        LambdaQueryWrapper<ExamQuestion> examQuestionWrapper = new LambdaQueryWrapper<>();
        examQuestionWrapper.eq(ExamQuestion::getExamId, examId);
        List<ExamQuestion> examQuestions = examQuestionMapper.selectList(examQuestionWrapper);
        List<Integer> quIds = examQuestions.stream()
                .map(ExamQuestion::getQuestionId)
                .collect(Collectors.toList());
        // 查询题干列表
        List<Question> questions = questionMapper.selectBatchIds(quIds);
        for (Question temp : questions) {
            // 创建返回对象
            ExamQuCollectVO examQuCollectVO = new ExamQuCollectVO();
            // 设置标题
            examQuCollectVO.setTitle(temp.getContent());
            examQuCollectVO.setQuType(temp.getQuType());

            // 查询试题选项
            LambdaQueryWrapper<Option> optionWrapper = new LambdaQueryWrapper<>();
            optionWrapper.eq(Option::getQuId, temp.getId());
            List<Option> options = optionMapper.selectList(optionWrapper);
            if (temp.getQuType() == 4) {
                examQuCollectVO.setOption(null);
            } else {
                examQuCollectVO.setOption(options);
            }


            // 设置是否正确
            LambdaQueryWrapper<ExamQuAnswer> examQuAnswerWrapper = new LambdaQueryWrapper<>();
            examQuAnswerWrapper.eq(ExamQuAnswer::getUserId, SecurityUtil.getUserId())
                    .eq(ExamQuAnswer::getExamId, examId)
                    .eq(ExamQuAnswer::getQuestionId, temp.getId());
            ExamQuAnswer examQuAnswer = examQuAnswerMapper.selectOne(examQuAnswerWrapper);
            // 如果某题没有作答
            if (examQuAnswer == null) {
                examQuCollectVO.setMyOption(null);
                examQuCollectVOS.add(examQuCollectVO);
                continue;
            }
            switch (temp.getQuType()) {
                case 1 -> {
                    // 设置自己的选项
                    LambdaQueryWrapper<Option> optionLambdaQueryWrapper = new LambdaQueryWrapper<>();
                    optionLambdaQueryWrapper.eq(Option::getId, examQuAnswer.getAnswerId());
                    Option op1 = optionMapper.selectOne(optionLambdaQueryWrapper);
                    examQuCollectVO.setMyOption(Integer.toString(op1.getSort()));

                }
                case 2 -> {
                    // 将回答id解析为列表
                    String answerId = examQuAnswer.getAnswerId();
                    List<Integer> opIds = Arrays.stream(answerId.split(","))
                            .map(Integer::parseInt)
                            .toList();
                    // 添加选项顺序
                    List<Integer> sorts = new ArrayList<>();
                    for (Integer opId : opIds) {
                        LambdaQueryWrapper<Option> optionLambdaQueryWrapper = new LambdaQueryWrapper<>();
                        optionLambdaQueryWrapper.eq(Option::getId, opId);
                        Option option = optionMapper.selectOne(optionLambdaQueryWrapper);
                        sorts.add(option.getSort());
                    }
                    // 设置自己选的选项，选项为顺序 1为A，2为B...
                    List<String> shortList = sorts.stream().map(String::valueOf).collect(Collectors.toList());
                    String myOption = String.join(",", shortList);
                    examQuCollectVO.setMyOption(myOption);
                }
                case 3 -> {
                    // 查询自己的的选项
                    LambdaQueryWrapper<Option> optionLambdaQueryWrapper = new LambdaQueryWrapper<>();
                    optionLambdaQueryWrapper.eq(Option::getId, examQuAnswer.getAnswerId());
                    Option op1 = optionMapper.selectOne(optionLambdaQueryWrapper);
                    examQuCollectVO.setMyOption(Integer.toString(op1.getSort()));

                }
                case 4 -> {
                    examQuCollectVO.setMyOption(examQuAnswer.getAnswerContent());
                }
                default -> {
                }
            }
            ;
            examQuCollectVOS.add(examQuCollectVO);
        }
        return Result.success("查询成功", examQuCollectVOS);
    }

    @Override
    public Result<ExamDetailVO> getDetail(Integer examId) {
        // 查询考试详情信息
        Exam exam = this.getById(examId);
        // 实体转换
        ExamDetailVO examDetailVO = examConverter.examToExamDetailVO(exam);
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getId, examDetailVO.getUserId());
        User user = userMapper.selectOne(userLambdaQueryWrapper);
        examDetailVO.setUsername(user.getUserName());
        return Result.success("查询成功", examDetailVO);
    }

    @Override
    public Result<Integer> addCheat(Integer examId) {
        // 检查是否正在考试
        // if(isUserTakingExam(examId)){
        //     return Result.failed("没有考试在进行");
        // }
        LambdaQueryWrapper<UserExamsScore> userExamsScoreLambdaQuery = new LambdaQueryWrapper<>();
        userExamsScoreLambdaQuery.eq(UserExamsScore::getExamId, examId)
                .eq(UserExamsScore::getUserId, SecurityUtil.getUserId());
        UserExamsScore userExamsScore = userExamsScoreMapper.selectOne(userExamsScoreLambdaQuery);
        Exam exam = this.getById(examId);
        // 操作次数，自动交卷
        if (userExamsScore.getCount() >= exam.getMaxCount()) {
            this.handExam(examId);
            return Result.success("已超过最大切屏次数，已自动交卷", 1);
        }
        LambdaUpdateWrapper<UserExamsScore> userExamsScoreLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        userExamsScoreLambdaUpdateWrapper.eq(UserExamsScore::getExamId, examId)
                .eq(UserExamsScore::getUserId, SecurityUtil.getUserId())
                .set(UserExamsScore::getCount, userExamsScore.getCount() + 1);
        int insert = userExamsScoreMapper.update(userExamsScoreLambdaUpdateWrapper);
        return Result.success("请勿切屏，最大切屏次数：" + exam.getMaxCount() + ",已切屏次数:" + (userExamsScore.getCount() + 1), 0);
    }

    @Override
    public Result<String> addAnswer(ExamQuAnswerAddForm examQuAnswerForm) {
        // 检查是否正在考试
        // if(isUserTakingExam(examQuAnswerForm.getExamId())){
        //     return Result.failed("没有考试在进行");
        // }
        // 查询试题类型
        LambdaQueryWrapper<Question> QuWrapper = new LambdaQueryWrapper<>();
        QuWrapper.eq(Question::getId, examQuAnswerForm.getQuId());
        Question qu = questionMapper.selectOne(QuWrapper);
        Integer quType = qu.getQuType();
        // 查询是否有记录
        LambdaQueryWrapper<ExamQuAnswer> examQuAnswerLambdaQueryWrapper = new LambdaQueryWrapper<>();
        examQuAnswerLambdaQueryWrapper.eq(ExamQuAnswer::getExamId, examQuAnswerForm.getExamId())
                .eq(ExamQuAnswer::getQuestionId, examQuAnswerForm.getQuId())
                .eq(ExamQuAnswer::getUserId, SecurityUtil.getUserId());
        List<ExamQuAnswer> existingAnswers = examQuAnswerMapper.selectList(examQuAnswerLambdaQueryWrapper);
        if (!existingAnswers.isEmpty()) {
            // 更新逻辑，这里根据题型合并处理逻辑
            return updateAnswerIfExists(examQuAnswerForm, quType);
        } else {
            // 插入逻辑，同样根据题型处理
            return insertNewAnswer(examQuAnswerForm, quType);
        }
    }

    @Override
    public Result<String> insertNewAnswer(ExamQuAnswerAddForm examQuAnswerForm, Integer quType) {
        // 根据试题类型进行修改
        ExamQuAnswer examQuAnswer = prepareExamQuAnswer(examQuAnswerForm, quType);
        return switch (quType) {
            case 1 -> {
                Option byId = optionService.getById(examQuAnswerForm.getAnswer());
                if (byId.getIsRight() == 1) {
                    examQuAnswer.setIsRight(1);
                    examQuAnswerMapper.insert(examQuAnswer);
                    yield Result.success("请求成功");
                } else {
                    examQuAnswer.setIsRight(0);
                    examQuAnswerMapper.insert(examQuAnswer);
                    yield Result.success("请求成功");
                }
            }
            case 2 -> {
                // 查找正确答案
                LambdaQueryWrapper<Option> optionWrapper = new LambdaQueryWrapper<>();
                optionWrapper.eq(Option::getIsRight, 1)
                        .eq(Option::getQuId, examQuAnswerForm.getQuId());
                List<Option> examQuAnswers = optionMapper.selectList(optionWrapper);
                // 解析用户作答
                List<Integer> quIds = Arrays.stream(examQuAnswerForm.getAnswer().split(","))
                        .map(Integer::parseInt)
                        .toList();
                // 判读啊是否正确
                for (Option temp : examQuAnswers) {
                    if (!quIds.contains(temp.getId())) {
                        examQuAnswer.setIsRight(0);
                        examQuAnswerMapper.insert(examQuAnswer);
                        yield Result.success("回答错误");
                    }
                }
                examQuAnswer.setIsRight(1);
                examQuAnswerMapper.insert(examQuAnswer);
                yield Result.success("回答正确");
            }
            case 3 -> {
                Option byId = optionService.getById(examQuAnswerForm.getAnswer());
                if (byId.getIsRight() == 1) {
                    examQuAnswer.setIsRight(1);
                    examQuAnswerMapper.insert(examQuAnswer);
                    yield Result.success("请求成功");
                } else {
                    examQuAnswer.setIsRight(0);
                    examQuAnswerMapper.insert(examQuAnswer);
                    yield Result.success("请求成功");
                }
            }
            case 4 -> {
                LambdaQueryWrapper<Option> optionLambdaQueryWrapper = new LambdaQueryWrapper<>();
                optionLambdaQueryWrapper.eq(Option::getQuId, examQuAnswerForm.getQuId());
                Option option = optionMapper.selectOne(optionLambdaQueryWrapper);
                if (option.getContent().equals(examQuAnswerForm.getAnswer())) {
                    examQuAnswer.setIsRight(1);
                } else {
                    examQuAnswer.setIsRight(0);
                }
                examQuAnswerMapper.insert(examQuAnswer);
                yield Result.success("请求成功");
            }
            default -> {
                yield Result.failed("请求错误，请联系管理员解决");
            }
        };
    }

    @Override
    public Result<String> updateAnswerIfExists(ExamQuAnswerAddForm examQuAnswerForm, Integer quType) {
        // 根据试题类型进行修改
        return switch (quType) {
            case 1 -> {
                Option byId = optionService.getById(examQuAnswerForm.getAnswer());
                if (byId == null) {
                    yield Result.failed("数据库中不存在该试题，请联系管理员解决");
                }
                if (byId.getIsRight() == 1) {
                    LambdaUpdateWrapper<ExamQuAnswer> examQuAnswerWrapper = new LambdaUpdateWrapper<>();
                    examQuAnswerWrapper.eq(ExamQuAnswer::getUserId, SecurityUtil.getUserId())
                            .eq(ExamQuAnswer::getExamId, examQuAnswerForm.getExamId())
                            .eq(ExamQuAnswer::getQuestionId, examQuAnswerForm.getQuId())
                            .set(ExamQuAnswer::getIsRight, 1)
                            .set(ExamQuAnswer::getAnswerId, examQuAnswerForm.getAnswer());
                    examQuAnswerMapper.update(examQuAnswerWrapper);
                    yield Result.success("请求成功");
                } else {
                    LambdaUpdateWrapper<ExamQuAnswer> examQuAnswerLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                    examQuAnswerLambdaUpdateWrapper.eq(ExamQuAnswer::getUserId, SecurityUtil.getUserId())
                            .eq(ExamQuAnswer::getExamId, examQuAnswerForm.getExamId())
                            .eq(ExamQuAnswer::getQuestionId, examQuAnswerForm.getQuId())
                            .set(ExamQuAnswer::getIsRight, 0)
                            .set(ExamQuAnswer::getAnswerId, examQuAnswerForm.getAnswer());
                    examQuAnswerMapper.update(examQuAnswerLambdaUpdateWrapper);
                    yield Result.success("请求成功");
                }

            }
            case 2 -> {
                // 查找正确答案
                LambdaQueryWrapper<Option> optionWrapper = new LambdaQueryWrapper<>();
                optionWrapper.eq(Option::getIsRight, 1)
                        .eq(Option::getQuId, examQuAnswerForm.getQuId());
                List<Option> examQuAnswers = optionMapper.selectList(optionWrapper);
                if (examQuAnswers.isEmpty()) {
                    yield Result.failed("该题正确答案选项不存在");
                }
                // 解析用户作答
                List<Integer> quIds = Arrays.stream(examQuAnswerForm.getAnswer().split(","))
                        .map(Integer::parseInt)
                        .toList();
                // 判读啊是否正确
                for (Option temp : examQuAnswers) {
                    if (!quIds.contains(temp.getId())) {
                        LambdaUpdateWrapper<ExamQuAnswer> examQuAnswerWrapper = new LambdaUpdateWrapper<>();
                        examQuAnswerWrapper.eq(ExamQuAnswer::getUserId, SecurityUtil.getUserId())
                                .eq(ExamQuAnswer::getExamId, examQuAnswerForm.getExamId())
                                .eq(ExamQuAnswer::getQuestionId, examQuAnswerForm.getQuId())
                                .set(ExamQuAnswer::getIsRight, 0)
                                .set(ExamQuAnswer::getAnswerId, examQuAnswerForm.getAnswer());
                        examQuAnswerMapper.update(examQuAnswerWrapper);
                        yield Result.success("回答错误");
                    }
                }
                LambdaUpdateWrapper<ExamQuAnswer> examQuAnswerWrapper = new LambdaUpdateWrapper<>();
                examQuAnswerWrapper.eq(ExamQuAnswer::getUserId, SecurityUtil.getUserId())
                        .eq(ExamQuAnswer::getExamId, examQuAnswerForm.getExamId())
                        .eq(ExamQuAnswer::getQuestionId, examQuAnswerForm.getQuId())
                        .set(ExamQuAnswer::getIsRight, 1)
                        .set(ExamQuAnswer::getAnswerId, examQuAnswerForm.getAnswer());
                examQuAnswerMapper.update(examQuAnswerWrapper);
                yield Result.success("回答正确");
            }
            case 3 -> {
                Option byId = optionService.getById(examQuAnswerForm.getAnswer());
                if (byId == null) {
                    yield Result.failed("数据库中不存在该试题，请联系管理员解决");
                }
                if (byId.getIsRight() == 1) {
                    LambdaUpdateWrapper<ExamQuAnswer> examQuAnswerWrapper = new LambdaUpdateWrapper<>();
                    examQuAnswerWrapper.eq(ExamQuAnswer::getUserId, SecurityUtil.getUserId())
                            .eq(ExamQuAnswer::getExamId, examQuAnswerForm.getExamId())
                            .eq(ExamQuAnswer::getQuestionId, examQuAnswerForm.getQuId())
                            .set(ExamQuAnswer::getIsRight, 1)
                            .set(ExamQuAnswer::getAnswerId, examQuAnswerForm.getAnswer());
                    examQuAnswerMapper.update(examQuAnswerWrapper);
                    yield Result.success("请求成功");
                } else {
                    LambdaUpdateWrapper<ExamQuAnswer> examQuAnswerLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                    examQuAnswerLambdaUpdateWrapper.eq(ExamQuAnswer::getUserId, SecurityUtil.getUserId())
                            .eq(ExamQuAnswer::getExamId, examQuAnswerForm.getExamId())
                            .eq(ExamQuAnswer::getQuestionId, examQuAnswerForm.getQuId())
                            .set(ExamQuAnswer::getIsRight, 0)
                            .set(ExamQuAnswer::getAnswerId, examQuAnswerForm.getAnswer());
                    examQuAnswerMapper.update(examQuAnswerLambdaUpdateWrapper);
                    yield Result.success("请求成功");
                }
            }
            case 4 -> {
                LambdaUpdateWrapper<ExamQuAnswer> examQuAnswerLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                examQuAnswerLambdaUpdateWrapper.eq(ExamQuAnswer::getUserId, SecurityUtil.getUserId())
                        .eq(ExamQuAnswer::getExamId, examQuAnswerForm.getExamId())
                        .eq(ExamQuAnswer::getQuestionId, examQuAnswerForm.getQuId())
                        .set(ExamQuAnswer::getAnswerContent, examQuAnswerForm.getAnswer());
                examQuAnswerMapper.update(examQuAnswerLambdaUpdateWrapper);
                yield Result.success("请求成功");
            }
            default -> Result.failed("请求错误，请联系管理员解决");
        };
    }

    @Override
    public ExamQuAnswer prepareExamQuAnswer(ExamQuAnswerAddForm form, Integer quType) {
        // 表单转换实体
        ExamQuAnswer examQuAnswer = examQuAnswerConverter.formToEntity(form);
        if (quType == 4) {
            examQuAnswer.setAnswerContent(form.getAnswer());
        } else {
            examQuAnswer.setAnswerId(form.getAnswer());
        }

        examQuAnswer.setUserId(SecurityUtil.getUserId());
        examQuAnswer.setQuestionType(quType);
        return examQuAnswer;
    }

    @Override
    public boolean isUserTakingExam(Integer examId) {
        // 判断是否正在考试
        LambdaQueryWrapper<UserExamsScore> userExamsScoreLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userExamsScoreLambdaQueryWrapper.eq(UserExamsScore::getUserId, SecurityUtil.getUserId())
                .eq(UserExamsScore::getExamId, examId)
                .eq(UserExamsScore::getState, 0);
        List<UserExamsScore> userExamsScores = userExamsScoreMapper.selectList(userExamsScoreLambdaQueryWrapper);
        if (userExamsScores.size() == 0) {
            return false;
        }
        return true;
    }

    @Override
    public Result<IPage<ExamGradeListVO>> getGradeExamList(Integer pageNum, Integer pageSize, String title) {
        IPage<ExamGradeListVO> examPage = new Page<>(pageNum, pageSize);
        // 根据班级查找考试ID
        if ("role_student".equals(SecurityUtil.getRole())) {
            examPage = examGradeMapper.selectClassExam(examPage, SecurityUtil.getUserId(), title);
        } else if ("role_admin".equals(SecurityUtil.getRole())) {
            examPage = examGradeMapper.selectAdminClassExam(examPage, title);
        }
        // 根据考试id查找考试
        return Result.success("查询成功", examPage);
    }

    @Override
    @Transactional
    public Result<ExamQuDetailVO> handExam(Integer examId) {
        // 检查是否正在考试
        if (!isUserTakingExam(examId)) {
            return Result.failed("没有考试在进行");
        }
        // 获取当前时间
        LocalDateTime nowTime = LocalDateTime.now();
        // 查询考试表记录
        Exam examOne = this.getById(examId);
        // 判断交卷是否超时
        LocalDateTime endTime = examOne.getEndTime();
        if (endTime.isBefore(nowTime)) {
            return Result.failed("提交失败，已过交卷时间");
        }
        // 设置考试状态
        UserExamsScore userExamsScore = new UserExamsScore();
        userExamsScore.setUserScore(0);
        userExamsScore.setState(1);

        // 查询用户答题记录
        LambdaQueryWrapper<ExamQuAnswer> examQuAnswerLambdaQuery = new LambdaQueryWrapper<>();
        examQuAnswerLambdaQuery.eq(ExamQuAnswer::getUserId, SecurityUtil.getUserId())
                .eq(ExamQuAnswer::getExamId, examId);
        List<ExamQuAnswer> examQuAnswer = examQuAnswerMapper.selectList(examQuAnswerLambdaQuery);
        // 客观分
        List<UserBook> userBookArrayList = new ArrayList<>();
        for (ExamQuAnswer temp : examQuAnswer) {
            if (temp.getIsRight() == 1) {
                if (temp.getQuestionType() == 1) {
                    userExamsScore.setUserScore(userExamsScore.getUserScore() + examOne.getRadioScore());
                } else if (temp.getQuestionType() == 2) {
                    userExamsScore.setUserScore(userExamsScore.getUserScore() + examOne.getMultiScore());
                } else if (temp.getQuestionType() == 3) {
                    userExamsScore.setUserScore(userExamsScore.getUserScore() + examOne.getJudgeScore());
                }
            } else if (temp.getIsRight() == 0) {
                UserBook userBook = new UserBook();
                userBook.setExamId(examId);
                userBook.setUserId(SecurityUtil.getUserId());
                userBook.setQuId(temp.getQuestionId());
                userBook.setCreateTime(nowTime);
                userBookArrayList.add(userBook);
            }
        }
        if (!userBookArrayList.isEmpty()) {
            // 把打错的问题加入错题本
            userBookMapper.addUserBookList(userBookArrayList);
        }
        // 设置用户用时和提交试卷
        userExamsScore.setLimitTime(nowTime);
        // 开始时间
        LambdaQueryWrapper<UserExamsScore> userExamsScoreLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userExamsScoreLambdaQueryWrapper.eq(UserExamsScore::getUserId, SecurityUtil.getUserId())
                .eq(UserExamsScore::getExamId, examId);
        UserExamsScore userExamsScore1 = userExamsScoreMapper.selectOne(userExamsScoreLambdaQueryWrapper);
        LocalDateTime createTime = userExamsScore1.getCreateTime();
        long secondsDifference = Duration.between(createTime, nowTime).getSeconds();
        int differenceAsInteger = (int) secondsDifference;
        // 检查是否在Integer范围内
        // if (secondsDifference <= Integer.MAX_VALUE && secondsDifference >= Integer.MIN_VALUE)
        userExamsScore.setUserTime(differenceAsInteger);
        // 添加总分和状态
        LambdaUpdateWrapper<UserExamsScore> userExamsScoreLambdaUpdate = new LambdaUpdateWrapper<>();
        userExamsScoreLambdaUpdate.eq(UserExamsScore::getUserId, SecurityUtil.getUserId())
                .eq(UserExamsScore::getExamId, examId);
        userExamsScoreMapper.update(userExamsScore, userExamsScoreLambdaUpdate);
        // 判断是否有简答题
        if (examOne.getSaqCount() != 0) {
            LambdaUpdateWrapper<UserExamsScore> userExamsScoreLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            userExamsScoreLambdaUpdateWrapper.set(UserExamsScore::getWhetherMark, 0)
                    .eq(UserExamsScore::getExamId, examId)
                    .eq(UserExamsScore::getUserId, SecurityUtil.getUserId());
            userExamsScoreMapper.update(userExamsScoreLambdaUpdateWrapper);
            return Result.success("提交成功，待老师阅卷");
        }
        if (userExamsScore.getUserScore() >= examOne.getPassedScore()) {
            CertificateUser certificateUser = new CertificateUser();
            certificateUser.setCertificateId(examOne.getCertificateId());
            certificateUser.setUserId(SecurityUtil.getUserId());
            certificateUser.setExamId(examId);
            certificateUser.setCode(ClassTokenGenerator.generateClassToken(18));
            certificateUserMapper.insert(certificateUser);
        }
        // 查询有简答题是否回答
        Exam byId = this.getById(examId);
        if (byId.getSaqCount() > 0) {
            LambdaQueryWrapper<ExamQuAnswer> examQuAnswerLambdaQueryWrapper = new LambdaQueryWrapper<>();
            examQuAnswerLambdaQueryWrapper.eq(ExamQuAnswer::getUserId, SecurityUtil.getUserId())
                    .eq(ExamQuAnswer::getExamId, examId)
                    .eq(ExamQuAnswer::getQuestionType, 4);
            List<ExamQuAnswer> examQuAnswers = examQuAnswerMapper.selectList(examQuAnswerLambdaQueryWrapper);
            if (examQuAnswers.isEmpty()) {
                LambdaQueryWrapper<ExamQuestion> examQuestionLambdaQueryWrapper = new LambdaQueryWrapper<>();
                examQuestionLambdaQueryWrapper.eq(ExamQuestion::getExamId, examId)
                        .eq(ExamQuestion::getType, 4);
                List<ExamQuestion> examQuestions = examQuestionMapper.selectList(examQuestionLambdaQueryWrapper);
                examQuestions.forEach(temp -> {
                    ExamQuAnswer examQuAnswer1 = new ExamQuAnswer();
                    examQuAnswer1.setExamId(examId);
                    examQuAnswer1.setUserId(SecurityUtil.getUserId());
                    examQuAnswer1.setQuestionId(temp.getQuestionId());
                    examQuAnswer1.setQuestionType(temp.getType());
                    examQuAnswer1.setIsRight(-1);
                    examQuAnswerMapper.insert(examQuAnswer1);
                });
            }

        }

        LambdaUpdateWrapper<UserExamsScore> userExamsScoreLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        userExamsScoreLambdaUpdateWrapper.set(UserExamsScore::getWhetherMark, -1)
                .eq(UserExamsScore::getExamId, examId)
                .eq(UserExamsScore::getUserId, SecurityUtil.getUserId());
        userExamsScoreMapper.update(userExamsScoreLambdaUpdateWrapper);
        return Result.success("交卷成功");
    }

    @Override
    public Result<String> startExam(Integer examId) {

        // 检查是否正在考试

        if (isUserTakingExam(examId)) {
            return Result.failed("已经有考试正在进行");
        }
        LambdaQueryWrapper<UserExamsScore> userExamsScoreLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userExamsScoreLambdaQueryWrapper.eq(UserExamsScore::getUserId, SecurityUtil.getUserId())
                .eq(UserExamsScore::getExamId, examId);
        List<UserExamsScore> userExamsScores = userExamsScoreMapper.selectList(userExamsScoreLambdaQueryWrapper);
        if (!userExamsScores.isEmpty()) {
            return Result.failed("这场考试已考不能第二次考试");
        }
        Exam exam = this.getById(examId);
        // 添加用户考试记录
        UserExamsScore userExamsScore = new UserExamsScore();
        userExamsScore.setExamId(examId);
        userExamsScore.setTotalTime(exam.getExamDuration());
        userExamsScore.setState(0);
        int rows = userExamsScoreMapper.insert(userExamsScore);
        if (rows == 0) {
            return Result.failed("访问失败");
        }
        return Result.success("已开始考试");
    }
}
