package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.cache.QuContentCacheService;
import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.ExerciseConverter;
import cn.org.alan.exam.converter.RecordConverter;
import cn.org.alan.exam.mapper.*;
import cn.org.alan.exam.model.entity.*;
import cn.org.alan.exam.model.form.exercise.ExerciseFillAnswerFrom;
import cn.org.alan.exam.model.vo.question.QuContentShell;
import cn.org.alan.exam.model.vo.question.QuestionVO;
import cn.org.alan.exam.model.vo.exercise.AnswerInfoVO;
import cn.org.alan.exam.model.vo.exercise.QuestionSheetVO;
import cn.org.alan.exam.model.vo.record.ExamRecordDetailVO;
import cn.org.alan.exam.model.vo.record.ExamRecordVO;
import cn.org.alan.exam.model.vo.record.ExerciseRecordDetailVO;
import cn.org.alan.exam.model.vo.record.ExerciseRecordVO;
import cn.org.alan.exam.service.IExerciseRecordService;
import cn.org.alan.exam.service.IOptionService;
import cn.org.alan.exam.utils.BlankPlaceholderUtil;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考试记录服务实现类
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Service
public class ExerciseRecordServiceImpl extends ServiceImpl<ExerciseRecordMapper, ExerciseRecord>
        implements IExerciseRecordService {

    @Resource
    private QuestionMapper questionMapper;
    @Resource
    private ExamMapper examMapper;
    @Resource
    private RecordConverter recordConverter;
    @Resource
    private ExamQuestionMapper examQuestionMapper;
    @Resource
    private OptionMapper optionMapper;
    @Resource
    private ExamQuAnswerMapper examQuAnswerMapper;
    @Resource
    private IOptionService optionService;
    @Resource
    private UserExerciseRecordMapper userExerciseRecordMapper;
    @Resource
    private RepoMapper repoMapper;
    @Resource
    private ExerciseConverter exerciseConverter;
    @Resource
    private ExerciseRecordMapper exerciseRecordMapper;
    @Resource
    private GradeExerciseMapper gradeExerciseMapper;
    @Resource
    private UserGradeMapper userGradeMapper;
    @Resource
    private QuContentCacheService quContentCacheService;


    @Override
    public Result<List<QuestionSheetVO>> getQuestionSheet(Integer repoId, Integer quType) {
        assertStudentCanExerciseRepo(repoId);
        List<QuestionSheetVO> list = questionMapper.selectQuestionSheet(repoId, quType, SecurityUtil.getUserId());
        return Result.success("获取获取试题答题卡列表成功", list);
    }

    @Override
    public Result<IPage<ExamRecordVO>> getExamRecordPage(Integer pageNum, Integer pageSize, String examName, Boolean isASC) {
        // 创建page对象
        Page<ExamRecordVO> examPage = new Page<>(pageNum, pageSize);
        
        // 获取当前用户ID和角色
        Integer userId = SecurityUtil.getUserId();
        Integer roleCode = SecurityUtil.getRoleCode();
        
        // 根据不同角色查询不同的试卷
        if (roleCode==3) {
            // 管理员查询所有已作答的试卷
            examPage = examMapper.getAllExamRecordPage(examPage, examName, isASC);
        } else if (roleCode==2) {
            // 教师查询自己创建的试卷
            examPage = examMapper.getTeacherExamRecordPage(examPage, userId, examName, isASC);
        } else {
            // 学生查询自己的试卷
            examPage = examMapper.getExamRecordPage(examPage, userId, examName, isASC);
        }
        
        return Result.success("分页查询已考试试卷成功", examPage);
    }

    @Override
    public Result<List<ExamRecordDetailVO>> getExamRecordDetail(Integer examId, Integer userId) {
        if (userId == null) {
            userId = SecurityUtil.getUserId();
        }
        List<ExamRecordDetailVO> examRecordDetailVOS = new ArrayList<>();
        LambdaQueryWrapper<ExamQuestion> examQuestionWrapper = new LambdaQueryWrapper<>();
        examQuestionWrapper.eq(ExamQuestion::getExamId, examId)
                .orderByAsc(ExamQuestion::getSort);
        List<ExamQuestion> examQuestions = examQuestionMapper.selectList(examQuestionWrapper);
        if (examQuestions == null || examQuestions.isEmpty()) {
            return Result.success("查询考试的信息成功", examRecordDetailVOS);
        }
        List<Integer> quIds = examQuestions.stream()
                .map(ExamQuestion::getQuestionId)
                .collect(Collectors.toList());
        List<Question> questions = questionMapper.selectBatchIds(quIds);
        Map<Integer, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q, (a, b) -> a));

        // 批量查选项，避免按题循环查询
        List<Option> allOptions = optionMapper.selectList(new LambdaQueryWrapper<Option>()
                .in(Option::getQuId, quIds)
                .orderByAsc(Option::getSort));
        Map<Integer, List<Option>> optionsByQuId = allOptions.stream()
                .collect(Collectors.groupingBy(Option::getQuId, LinkedHashMap::new, Collectors.toList()));
        Map<Integer, Option> optionById = allOptions.stream()
                .collect(Collectors.toMap(Option::getId, o -> o, (a, b) -> a));

        // 批量查该考生作答
        List<ExamQuAnswer> answerList = examQuAnswerMapper.selectList(new LambdaQueryWrapper<ExamQuAnswer>()
                .eq(ExamQuAnswer::getExamId, examId)
                .eq(ExamQuAnswer::getUserId, userId)
                .in(ExamQuAnswer::getQuestionId, quIds));
        Map<Integer, ExamQuAnswer> answerByQuId = answerList.stream()
                .collect(Collectors.toMap(ExamQuAnswer::getQuestionId, a -> a, (a, b) -> a));

        for (ExamQuestion examQuestion : examQuestions) {
            Question temp = questionMap.get(examQuestion.getQuestionId());
            if (temp == null) {
                continue;
            }
            ExamRecordDetailVO examRecordDetailVO = new ExamRecordDetailVO();
            examRecordDetailVO.setQuestionId(temp.getId());
            examRecordDetailVO.setImage(temp.getImage());
            examRecordDetailVO.setAudio(temp.getAudio());
            examRecordDetailVO.setTitle(temp.getContent());
            examRecordDetailVO.setQuType(temp.getQuType());
            examRecordDetailVO.setLevel(temp.getLevel());
            examRecordDetailVO.setScore(examQuestion.getScore());
            examRecordDetailVO.setAnalyse(temp.getAnalysis());

            List<Option> options = optionsByQuId.getOrDefault(temp.getId(), Collections.emptyList());
            Integer quType = temp.getQuType();

            if (quType != null && quType == 4) {
                examRecordDetailVO.setOption(null);
                if (!options.isEmpty()) {
                    examRecordDetailVO.setRightOption(options.get(0).getContent());
                }
            } else if (quType != null && quType == 5) {
                examRecordDetailVO.setOption(options);
                if (!options.isEmpty()) {
                    examRecordDetailVO.setRightOption(options.stream()
                            .map(Option::getContent)
                            .collect(Collectors.joining(BlankPlaceholderUtil.ANSWER_DELIMITER)));
                }
            } else {
                examRecordDetailVO.setOption(options);
                List<Integer> rightSorts = new ArrayList<>();
                for (Option opt : options) {
                    if (opt.getIsRight() != null && opt.getIsRight() == 1) {
                        rightSorts.add(opt.getSort());
                    }
                }
                examRecordDetailVO.setRightOption(
                        rightSorts.stream().map(String::valueOf).collect(Collectors.joining(","))
                );
            }

            ExamQuAnswer examQuAnswer = answerByQuId.get(temp.getId());
            if (examQuAnswer == null) {
                examRecordDetailVO.setMyOption(null);
                examRecordDetailVO.setIsRight(-1);
                examRecordDetailVOS.add(examRecordDetailVO);
                continue;
            }
            if (quType == null) {
                examRecordDetailVOS.add(examRecordDetailVO);
                continue;
            }
            switch (quType) {
                case 1:
                case 3: {
                    if (StringUtils.isBlank(examQuAnswer.getAnswerId())) {
                        examRecordDetailVO.setMyOption(null);
                        examRecordDetailVO.setIsRight(0);
                        break;
                    }
                    Option op = optionById.get(Integer.valueOf(examQuAnswer.getAnswerId()));
                    if (op == null) {
                        examRecordDetailVO.setMyOption(null);
                        examRecordDetailVO.setIsRight(0);
                        break;
                    }
                    examRecordDetailVO.setMyOption(Integer.toString(op.getSort()));
                    examRecordDetailVO.setIsRight(op.getIsRight() != null && op.getIsRight() == 1 ? 1 : 0);
                    break;
                }
                case 2: {
                    if (StringUtils.isBlank(examQuAnswer.getAnswerId())) {
                        examRecordDetailVO.setMyOption(null);
                        examRecordDetailVO.setIsRight(0);
                        break;
                    }
                    List<Integer> opIds = Arrays.stream(examQuAnswer.getAnswerId().split(","))
                            .map(String::trim)
                            .filter(StringUtils::isNotBlank)
                            .map(Integer::parseInt)
                            .collect(Collectors.toList());
                    List<Integer> sorts = new ArrayList<>();
                    for (Integer opId : opIds) {
                        Option option = optionById.get(opId);
                        if (option != null && option.getSort() != null) {
                            sorts.add(option.getSort());
                        }
                    }
                    examRecordDetailVO.setMyOption(sorts.stream().map(String::valueOf).collect(Collectors.joining(",")));
                    List<Option> rightOptions = options.stream()
                            .filter(o -> o.getIsRight() != null && o.getIsRight() == 1)
                            .collect(Collectors.toList());
                    boolean allMatch = !rightOptions.isEmpty()
                            && rightOptions.size() == opIds.size()
                            && rightOptions.stream().allMatch(r -> opIds.contains(r.getId()));
                    examRecordDetailVO.setIsRight(allMatch ? 1 : 0);
                    break;
                }
                case 4:
                    examRecordDetailVO.setMyOption(examQuAnswer.getAnswerContent());
                    examRecordDetailVO.setIsRight(-1);
                    break;
                case 5:
                    examRecordDetailVO.setMyOption(examQuAnswer.getAnswerContent());
                    examRecordDetailVO.setIsRight(examQuAnswer.getIsRight() == null ? -1 : examQuAnswer.getIsRight());
                    break;
                default:
                    break;
            }
            examRecordDetailVOS.add(examRecordDetailVO);
        }
        return Result.success("查询考试的信息成功", examRecordDetailVOS);
    }

    @Override
    public Result<IPage<ExerciseRecordVO>> getExerciseRecordPage(Integer pageNum, Integer pageSize ,String repoName) {
        // 创建page对象
        Page<Repo> repoPage = new Page<>(pageNum, pageSize);
        // 查询该用户已考试的考试id
        Integer userId = SecurityUtil.getUserId();
        Page<Repo> exercisePageResult = repoMapper.selectUserExerciseRecord(repoPage,userId,repoName);
        // 实体转换
        Page<ExerciseRecordVO> exerciseRecordVOPage = recordConverter.pageRepoEntityToVo(exercisePageResult);
        return Result.success("查询成功", exerciseRecordVOPage);
    }

    @Override
    public Result<List<ExerciseRecordDetailVO>> getExerciseRecordDetail(Integer exerciseId) {
        // 1、题干 2、选项 3、自己的答案 4、正确的答案 5、是否正确 6、试题分析
        List<ExerciseRecordDetailVO> exerciseRecordDetailVOS = new ArrayList<>();
        // 查询该考试的试题
        LambdaQueryWrapper<Question> questionLambdaQueryWrapper = new LambdaQueryWrapper<>();
        questionLambdaQueryWrapper.eq(Question::getRepoId, exerciseId);
        List<Question> questions1 = questionMapper.selectList(questionLambdaQueryWrapper);
        for (Question temp : questions1) {
            ExerciseRecordDetailVO exerciseRecordDetailVO = new ExerciseRecordDetailVO();
            exerciseRecordDetailVO.setImage(temp.getImage());
            exerciseRecordDetailVO.setAudio(temp.getAudio());
            exerciseRecordDetailVO.setTitle(temp.getContent());
            exerciseRecordDetailVO.setAnalyse(temp.getAnalysis());
            exerciseRecordDetailVO.setQuType(temp.getQuType());
            // 查询试题选项
            LambdaQueryWrapper<Option> optionWrapper = new LambdaQueryWrapper<>();
            optionWrapper.eq(Option::getQuId, temp.getId());
            List<Option> options = optionMapper.selectList(optionWrapper);
            options.sort(Comparator.comparing(option -> option.getSort() == null ? 0 : option.getSort()));
            if (temp.getQuType() == 4) {
                exerciseRecordDetailVO.setOption(null);
            } else {
                exerciseRecordDetailVO.setOption(options);
            }

            if (temp.getQuType() == 4 && options.size() > 0) {
                exerciseRecordDetailVO.setRightOption(options.get(0).getContent());
            } else {
                String current = "";
                ArrayList<Integer> strings = new ArrayList<>();
                for (Option temp1 : options) {
                    if (temp1.getIsRight() == 1) {
                        strings.add(temp1.getSort());
                    }
                }
                List<String> stringList = strings.stream().map(String::valueOf).collect(Collectors.toList());
                String result = String.join(",", stringList);

                exerciseRecordDetailVO.setRightOption(result);
            }
            LambdaQueryWrapper<ExerciseRecord> exerciseRecordLambdaQueryWrapper = new LambdaQueryWrapper<>();
            exerciseRecordLambdaQueryWrapper.eq(ExerciseRecord::getUserId, SecurityUtil.getUserId())
                    .eq(ExerciseRecord::getRepoId, exerciseId)
                    .eq(ExerciseRecord::getQuestionId, temp.getId());
            ExerciseRecord exerciseRecord = exerciseRecordMapper.selectOne(exerciseRecordLambdaQueryWrapper);

            // 如果某题没有作答
            if (exerciseRecord == null) {
                exerciseRecordDetailVO.setMyOption(null);
                exerciseRecordDetailVO.setIsRight(-1);
                exerciseRecordDetailVOS.add(exerciseRecordDetailVO);
                continue;
            }
            switch (temp.getQuType()) {
                case 1:
                    // 设置自己的选项
                    LambdaQueryWrapper<Option> optionLambdaQueryWrapper1 = new LambdaQueryWrapper<>();
                    optionLambdaQueryWrapper1.eq(Option::getId, exerciseRecord.getAnswer());
                    Option op1 = optionMapper.selectOne(optionLambdaQueryWrapper1);
                    exerciseRecordDetailVO.setMyOption(Integer.toString(op1.getSort()));
                    // 设置是否正确
                    Option byId1 = optionService.getById(exerciseRecord.getAnswer());
                    if (byId1.getIsRight() == 1) {
                        exerciseRecordDetailVO.setIsRight(1);
                    } else {
                        exerciseRecordDetailVO.setIsRight(0);
                    }
                    break;
                case 2:
                    // 将回答id解析为列表
                    String answerId = exerciseRecord.getAnswer();
                    List<Integer> opIds = Arrays.stream(answerId.split(","))
                            .map(Integer::parseInt)
                            .collect(Collectors.toList());
                    // 添加选项顺序
                    List<Integer> sorts = new ArrayList<>();
                    for (Integer opId : opIds) {
                        LambdaQueryWrapper<Option> optionLambdaQueryWrapper2 = new LambdaQueryWrapper<>();
                        optionLambdaQueryWrapper2.eq(Option::getId, opId);
                        Option option = optionMapper.selectOne(optionLambdaQueryWrapper2);
                        sorts.add(option.getSort());
                    }
                    sorts.sort(Comparator.naturalOrder());
                    // 设置自己选的选项，选项为顺序 1为A，2为B...
                    List<String> shortList = sorts.stream().map(String::valueOf).collect(Collectors.toList());
                    String myOption = String.join(",", shortList);
                    exerciseRecordDetailVO.setMyOption(myOption);
                    // 查找正确答案
                    LambdaQueryWrapper<Option> optionWrapper1 = new LambdaQueryWrapper<>();
                    optionWrapper1.eq(Option::getIsRight, 1)
                            .eq(Option::getQuId, temp.getId());
                    List<Option> examQuAnswers = optionMapper.selectList(optionWrapper1);
                    // 判断是否正确
                    exerciseRecordDetailVO.setIsRight(1);
                    for (Option temp1 : examQuAnswers) {
                        boolean contains = opIds.contains(temp1.getId());
                        if (!contains) {
                            // 只要有一个正确答案不在用户选择中则判断为错误
                            exerciseRecordDetailVO.setIsRight(0);
                            break;
                        }
                    }
                    break;
                case 3:
                    // 查询自己的的选项
                    LambdaQueryWrapper<Option> optionLambdaQueryWrapper3 = new LambdaQueryWrapper<>();
                    optionLambdaQueryWrapper3.eq(Option::getId, exerciseRecord.getAnswer());
                    Option op3 = optionMapper.selectOne(optionLambdaQueryWrapper3);
                    exerciseRecordDetailVO.setMyOption(Integer.toString(op3.getSort()));
                    // 查询是否正确
                    Option byId3 = optionService.getById(exerciseRecord.getAnswer());
                    if (byId3.getIsRight() == 1) {
                        exerciseRecordDetailVO.setIsRight(1);
                    } else {
                        exerciseRecordDetailVO.setIsRight(0);
                    }
                    break;
                case 4:
                    exerciseRecordDetailVO.setMyOption(null);
                    exerciseRecordDetailVO.setIsRight(-1);
                    break;
                default:
                    break;
            }
            exerciseRecordDetailVOS.add(exerciseRecordDetailVO);
        }
        return Result.success("查询刷题详情成功", exerciseRecordDetailVOS);
    }

    @Override
    @Transactional
    public Result<QuestionVO> fillAnswer(ExerciseFillAnswerFrom exerciseFillAnswerFrom) {
        assertStudentCanExerciseRepo(exerciseFillAnswerFrom.getRepoId());
        ExerciseRecord exerciseRecord = exerciseConverter.fromToEntity(exerciseFillAnswerFrom);
        if (exerciseRecord.getAnswer() == null) {
            exerciseRecord.setAnswer("");
        }
        //默认用户回答正确
        boolean flag = true;
        exerciseRecord.setIsRight(1);

        Integer quType = exerciseFillAnswerFrom.getQuType();
        //对客观题做题正确与否校验（未作答视为错误）；简答跳过；填空按空比对
        if (quType != null && quType == 4) {
            // 简答题不自动判分
        } else if (quType != null && quType == 5) {
            List<Option> fillOptions = optionMapper.selectAllByQuestionId(exerciseRecord.getQuestionId());
            fillOptions.sort(Comparator.comparing(o -> o.getSort() == null ? 0 : o.getSort()));
            List<String> standards = fillOptions.stream().map(Option::getContent).collect(Collectors.toList());
            List<String> userAnswers = BlankPlaceholderUtil.splitAnswers(exerciseRecord.getAnswer());
            int correct = BlankPlaceholderUtil.countCorrect(userAnswers, standards);
            flag = !standards.isEmpty() && correct == standards.size();
            exerciseRecord.setIsRight(flag ? 1 : 0);
        } else {
            String answer = exerciseRecord.getAnswer();
            if (StringUtils.isBlank(answer)) {
                flag = false;
                exerciseRecord.setIsRight(0);
            } else {
                List<Integer> options = Arrays.stream(answer.split(","))
                        .map(String::trim)
                        .filter(StringUtils::isNotBlank)
                        .map(Integer::parseInt)
                        .collect(java.util.stream.Collectors.toList());
                List<Integer> rightOptions = new ArrayList<>();
                optionMapper.selectAllByQuestionId(exerciseRecord.getQuestionId()).forEach(option -> {
                    if (option.getIsRight() == 1) {
                        rightOptions.add(option.getId());
                    }
                });
                if (options.isEmpty() || options.size() != rightOptions.size()) {
                    flag = false;
                } else {
                    for (Integer option : options) {
                        if (!rightOptions.contains(option)) {
                            flag = false;
                            break;
                        }
                    }
                }
                exerciseRecord.setIsRight(flag ? 1 : 0);
            }
        }
        if (flag) {
            exerciseRecord.setIsRight(1);
        } else {
            exerciseRecord.setIsRight(0);
        }
        //对是否第一次该题判断
        LambdaQueryWrapper<ExerciseRecord> exerciseRecordLambdaQueryWrapper = new LambdaQueryWrapper<ExerciseRecord>()
                .eq(ExerciseRecord::getUserId, SecurityUtil.getUserId())
                .eq(ExerciseRecord::getRepoId, exerciseRecord.getRepoId())
                .eq(ExerciseRecord::getQuestionId, exerciseRecord.getQuestionId());
        ExerciseRecord databaseExerciseRecord = exerciseRecordMapper.selectOne(exerciseRecordLambdaQueryWrapper);
        boolean exercised = !Optional.ofNullable(databaseExerciseRecord).isPresent();
        if (exercised) {
            //未做过该题，新增记录
            exerciseRecordMapper.insert(exerciseRecord);
            //获取该题库填作答记录
            LambdaQueryWrapper<UserExerciseRecord> exerciseRecordWrapper = new LambdaQueryWrapper<UserExerciseRecord>()
                    .eq(UserExerciseRecord::getUserId, SecurityUtil.getUserId())
                    .eq(UserExerciseRecord::getRepoId, exerciseRecord.getRepoId());
            UserExerciseRecord userExerciseRecord = userExerciseRecordMapper.selectOne(exerciseRecordWrapper);

            if (!Optional.ofNullable(userExerciseRecord).isPresent()) {
                //该题库用户首次刷题，添加一条记录
                LambdaQueryWrapper<Question> questionWrapper = new LambdaQueryWrapper<Question>()
                        .eq(Question::getRepoId, exerciseRecord.getRepoId());
                int totalCount = questionMapper.selectCount(questionWrapper).intValue();
                UserExerciseRecord insertUserExerciseRecord = new UserExerciseRecord();
                insertUserExerciseRecord.setExerciseCount(1);
                insertUserExerciseRecord.setRepoId(exerciseRecord.getRepoId());
                insertUserExerciseRecord.setTotalCount(totalCount);
                userExerciseRecordMapper.insert(insertUserExerciseRecord);
            } else {
                //修改题库总数，避免后续新增试题
                LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<Question>()
                        .eq(Question::getId, exerciseRecord.getRepoId());

                //该题库非首次刷题，修改刷题数
                UserExerciseRecord updateUserExerciseRecord = new UserExerciseRecord();
                updateUserExerciseRecord.setTotalCount(questionMapper.selectCount(wrapper).intValue());
                updateUserExerciseRecord.setId(userExerciseRecord.getId());
                updateUserExerciseRecord.setExerciseCount(userExerciseRecord.getExerciseCount() + 1);
                userExerciseRecordMapper.updateById(updateUserExerciseRecord);
            }
        } else {
            //已做过，修改答案
            exerciseRecord.setId(databaseExerciseRecord.getId());
            exerciseRecordMapper.updateById(exerciseRecord);
        }

        //获取试题信息，返回给用户
        QuestionVO questionVO = questionMapper.selectSingle(exerciseRecord.getQuestionId());

        //针对不同题型做出不同响应
        //主观题/填空题：返回含标准答案的试题信息供复习
        if (exerciseRecord.getQuestionType() != null
                && (exerciseRecord.getQuestionType() == 4 || exerciseRecord.getQuestionType() == 5)) {
            return Result.success(null, questionVO);
        }

        return flag ? Result.success("回答正确", questionVO) : Result.success("回答错误", questionVO);
    }

    @Override
    public Result<QuestionVO> getSingle(Integer id) {
        QuContentShell shell = quContentCacheService.getShell(id);
        if (shell == null || shell.getRepoId() == null) {
            throw new ServiceRuntimeException("试题不存在");
        }
        assertStudentCanExerciseRepo(shell.getRepoId());
        QuestionVO questionVO = quContentCacheService.toQuestionVO(shell);
        return Result.success("查询单题成功", questionVO);
    }

    @Override
    public Result<AnswerInfoVO> getAnswerInfo(Integer repoId, Integer quId) {
        assertStudentCanExerciseRepo(repoId);
        QuestionVO questionVO = questionMapper.selectSingle(quId);
        AnswerInfoVO answerInfoVO = exerciseConverter.quVOToAnswerInfoVO(questionVO);
        LambdaQueryWrapper<ExerciseRecord> exerciseRecordLambdaQueryWrapper = new LambdaQueryWrapper<ExerciseRecord>()
                .eq(ExerciseRecord::getRepoId, repoId)
                .eq(ExerciseRecord::getQuestionId, quId)
                .eq(ExerciseRecord::getUserId, SecurityUtil.getUserId());
        ExerciseRecord exerciseRecord = exerciseRecordMapper.selectOne(exerciseRecordLambdaQueryWrapper);
        if (exerciseRecord == null) {
            return Result.success("未作答", answerInfoVO);
        }
        answerInfoVO.setAnswerContent(exerciseRecord.getAnswer());
        return exerciseRecord.getIsRight() == 1 ?
                Result.success("回答正确", answerInfoVO) : Result.success("回答错误", answerInfoVO);

    }

    /**
     * 校验当前学生所在班级是否绑定了该题库，且题库已开启刷题
     */
    private void assertStudentCanExerciseRepo(Integer repoId) {
        if (repoId == null) {
            throw new ServiceRuntimeException("题库不存在");
        }
        List<Integer> gradeIds = userGradeMapper.getStudentGradeIdList(SecurityUtil.getUserId());
        if (gradeIds == null || gradeIds.isEmpty()) {
            throw new ServiceRuntimeException("请先加入班级后再刷题");
        }
        int count = gradeExerciseMapper.countStudentRepoAccess(repoId, gradeIds);
        if (count < 1) {
            throw new ServiceRuntimeException("无权刷该题库");
        }
    }
}
