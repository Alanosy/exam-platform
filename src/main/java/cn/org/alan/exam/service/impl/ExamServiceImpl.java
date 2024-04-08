package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.exception.AppException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.ExamConverter;
import cn.org.alan.exam.mapper.*;
import cn.org.alan.exam.model.entity.*;
import cn.org.alan.exam.model.form.exam.ExamAddForm;
import cn.org.alan.exam.model.form.exam.ExamUpdateForm;
import cn.org.alan.exam.model.vo.exam.*;
import cn.org.alan.exam.service.IExamQuAnswerService;
import cn.org.alan.exam.service.IExamService;
import cn.org.alan.exam.service.IQuestionService;
import cn.org.alan.exam.util.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 服务实现类
 *
 * @author WeiJin
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

    @Override
    @Transactional
    public Result<String> createExam(ExamAddForm examAddForm) {
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
        for (Map.Entry<Integer, Integer> entry : quTypeToCount.entrySet()) {
            // 获取当前试题类型、试题数量、考试id、试题分数
            Integer quType = entry.getKey();
            Integer count = entry.getValue();
            Integer examId = exam.getId();
            Integer quScore = quTypeToScore.get(quType);
            // 查询设置题库中，对应类型的试题id
            LambdaQueryWrapper<Question> typeQueryWrapper = new LambdaQueryWrapper<>();
            typeQueryWrapper.select(Question::getId)
                    .eq(Question::getQuType, quType)
                    .eq(Question::getRepoId, examAddForm.getRepoId());
            List<Question> questionsByType = questionMapper.selectList(typeQueryWrapper);
            if (questionsByType.size() < count) {
                throw new AppException("题库中类型为" + quType + "的题目数量不足" + count + "个！");
            }
            List<Integer> typeQuestionIds = questionsByType.stream().map(Question::getId).collect(Collectors.toList());
            Collections.shuffle(typeQuestionIds);
            List<Integer> sampledIds = typeQuestionIds.subList(0, count);
            // 插入试题
            int examQueRows = examQuestionMapper.insertQuestion(examId, quType, quScore, sampledIds);
            if (examQueRows == 0) {
                return Result.failed("创建失败");
            }
        }
        return Result.success("创建成功");
    }

    @Override
    @Transactional
    public Result<String> updateExam(ExamUpdateForm examUpdateForm, Integer id) {
        // 修改考试表记录
        LambdaQueryWrapper<Exam> examLambdaQuery = new LambdaQueryWrapper<>();
        examLambdaQuery.eq(Exam::getId, id);
        Exam examTemp = examMapper.selectOne(examLambdaQuery);
        int grossScore = examTemp.getRadioCount() * examUpdateForm.getRadioScore()
                + examTemp.getMultiCount() * examUpdateForm.getMultiScore()
                + examTemp.getJudgeCount() * examUpdateForm.getJudgeScore()
                + examTemp.getSaqCount() * examUpdateForm.getSaqScore();
        LambdaUpdateWrapper<Exam> examLambdaUpdate = new LambdaUpdateWrapper<>();
        examLambdaUpdate.eq(Exam::getId, id)
                .eq(Exam::getUserId, SecurityUtil.getUserId());
        Exam exam = examConverter.formToEntity(examUpdateForm);
        exam.setGrossScore(grossScore);
        int examRows = examMapper.update(exam, examLambdaUpdate);
        if (examRows == 0) {
            return Result.failed("修改失败");
        }
        // 修改考试班级
        String gradeIdsStr = examUpdateForm.getGradeIds();
        List<Integer> gradeIds = Arrays.stream(gradeIdsStr.split(","))
                .map(Integer::parseInt)
                .toList();
        int examGradeRows = examGradeMapper.delExamGrade(exam.getId());
        if (examGradeRows == 0) {
            return Result.failed("修改失败");
        }
        Integer gradeRows = examGradeMapper.addExamGrade(exam.getId(), gradeIds);
        if (gradeRows == 0) {
            return Result.failed("修改失败");
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
        int examRows = examMapper.delExam(examIds);
        if (examRows == 0) {
            return Result.failed("删除失败，删除考试表时失败");
        }
        // 删除考试班级记录
        int examGradeRows = examMapper.delExamGrade(examIds);
        if (examGradeRows == 0) {
            return Result.failed("删除失败，删除考试班级表时失败");
        }
        // 删除考试题库记录
        int examRepoRows = examMapper.delExamRepo(examIds);
        if (examRepoRows == 0) {
            return Result.failed("删除失败，删除考试题库表时失败");
        }
        // 删除考试试题记录
        int examQueRows = examMapper.delExamQue(examIds);
        if (examQueRows == 0) {
            return Result.failed("删除失败，删除考试试题表时失败");
        }
        return Result.success("删除成功");
    }

    @Override
    public Result<IPage<ExamVO>> getPagingExam(Integer pageNum, Integer pageSize, String title) {
        // 创建Page对象
        Page<Exam> page = new Page<>(pageNum, pageSize);
        // 开始查询
        LambdaQueryWrapper<Exam> examQuery = new LambdaQueryWrapper<>();
        examQuery.like(StringUtils.isNotBlank(title), Exam::getTitle, title)
                .eq(Exam::getUserId, SecurityUtil.getUserId());
        Page<Exam> examPage = examMapper.selectPage(page, examQuery);
        // 返回Result<IPage<ExamVO>>对象
        return Result.success("查询成功", examConverter.pageEntityToVo(examPage));
    }

    @Override
    public Result<List<ExamDetailRespVO>> getQuestionList(String id) {
        LambdaQueryWrapper<ExamQuestion> examQuestionLambdaQueryWrapper = new LambdaQueryWrapper<>();
        examQuestionLambdaQueryWrapper.eq(ExamQuestion::getExamId, id);
        List<ExamQuestion> examQuestions = examQuestionMapper.selectList(examQuestionLambdaQueryWrapper);
        List<ExamDetailRespVO> examDetailRespVO = examConverter.listEntityToExamDetailRespVO(examQuestions);
        return Result.success("查询成功",examDetailRespVO);
    }

    @Override
    public Result<ExamQuDetailVO> getQuestionSingle(String examId, String questionId) {
        ExamQuDetailVO examQuDetailVO = new ExamQuDetailVO();
        // 问题
        Question quById = questionService.getById(questionId);
        // 基本信息
        examQuDetailVO.setImage(quById.getImage());
        examQuDetailVO.setContent(quById.getContent());
        // 答案列表
        LambdaQueryWrapper<Option> optionLambdaQuery = new LambdaQueryWrapper<>();
        optionLambdaQuery.eq(Option::getQuId,questionId);
        List<Option> list = optionMapper.selectList(optionLambdaQuery);
        examQuDetailVO.setAnswerList(list);
        return Result.success("获取成功", examQuDetailVO);
    }

    @Override
    public Result<EXamQuCollectVO> getCollect(String id) {
        return null;
    }

    @Override
    public Result<ExamDetailVO> getDetail(String id) {
        return null;
    }

    @Override
    public Result<String> addCheat() {
        return null;
    }

    @Override
    public Result<ExamFillVO> addAnswer(ExamQuAnswerForm examQuAnswerForm) {

        // 未作答
        if (CollectionUtils.isEmpty(Collections.singleton(examQuAnswerForm.getAnswer()))
                && StringUtils.isBlank(examQuAnswerForm.getAnswer())) {
            return null;
        }
        // 查找答案列表
        List<ExamQuAnswer> list = examQuAnswerService.listForFill(examQuAnswerForm.getExamId(), examQuAnswerForm.getQuestionId());
        // 是否正确
        boolean right = true;
        // 更新正确答案
        for (ExamQuAnswer item : list) {

            if (examQuAnswerForm.getAnswers().contains(item.getId())) {
                item.setCheckout(1);
            } else {
                item.setCheckout(0);
            }

            // 有一个对不上就是错的
            if (item.getIsRight() != null && !item.getIsRight().equals(item.getCheckout())) {
                right = false;
            }
            examQuAnswerService.updateById(item);
        }
        //     //修改为已回答
        ExamQuAnswer equ = new ExamQuAnswer();
        equ.setQuestionId(examQuAnswerForm.getQuestionId());
        equ.setExamId(examQuAnswerForm.getExamId());
        equ.setIsRight(1);
        equ.setAnswerId(examQuAnswerForm.getAnswer());
        examQuAnswerService.updateByKey(equ);
        return null;
    }

    @Override
    public Result<IPage<ExamGradeVO>> getGradeExamList(String id, Integer pageNum, Integer pageSize) {
        Page page = new Page(pageNum, pageSize);
        LambdaQueryWrapper<ExamGrade> examLambda = new LambdaQueryWrapper<>();
        examLambda.eq(ExamGrade::getGradeId, id);
        Page result = examGradeMapper.selectPage(page, examLambda);
        return Result.success("查询成功", result);
    }
}
