package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.ExerciseConverter;
import cn.org.alan.exam.converter.QuestionConverter;
import cn.org.alan.exam.converter.RecordConverter;
import cn.org.alan.exam.mapper.*;
import cn.org.alan.exam.model.entity.*;
import cn.org.alan.exam.model.form.ExerciseFillAnswerFrom;
import cn.org.alan.exam.model.vo.QuestionVO;
import cn.org.alan.exam.model.vo.exam.ExamQuestionVO;
import cn.org.alan.exam.model.vo.exercise.AnswerInfoVO;
import cn.org.alan.exam.model.vo.exercise.QuestionSheetVO;
import cn.org.alan.exam.model.vo.record.ExamRecordDetailVO;
import cn.org.alan.exam.model.vo.record.ExamRecordVO;
import cn.org.alan.exam.model.vo.record.ExerciseRecordDetailVO;
import cn.org.alan.exam.model.vo.record.ExerciseRecordVO;
import cn.org.alan.exam.service.IExerciseRecordService;
import cn.org.alan.exam.service.IOptionService;
import cn.org.alan.exam.util.CacheClient;
import cn.org.alan.exam.util.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
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
    private CacheClient cacheClient;


    @Override
    public Result<List<QuestionSheetVO>> getQuestionSheet(Integer repoId, Integer quType) {
        List<QuestionSheetVO> list = questionMapper.selectQuestionSheet(repoId, quType, SecurityUtil.getUserId());
//
//        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<Question>()
//                .select(Question::getId, Question::getQuType, Question::getRepoId)
//                .eq(Question::getRepoId, repoId).eq(quType != null && quType != 0, Question::getQuType, quType);
//
//        List<QuestionSheetVO> list = questionConverter.listEntityToVO(questionMapper.selectList(wrapper));
//
//        //获取是否做过该试题
//        list.forEach(questionSheetVO -> {
//            LambdaQueryWrapper<ExerciseRecord> exerciseRecordLambdaQueryWrapper = new LambdaQueryWrapper<ExerciseRecord>()
//                    .eq(ExerciseRecord::getRepoId, questionSheetVO.getRepoId())
//                    .eq(ExerciseRecord::getQuestionId, questionSheetVO.getQuId())
//                    .eq(ExerciseRecord::getUserId, SecurityUtil.getUserId());
//
//            ExerciseRecord exerciseRecord = exerciseRecordMapper.selectOne(exerciseRecordLambdaQueryWrapper);
//
//            questionSheetVO.setExercised(Optional.ofNullable(exerciseRecord).isEmpty() ? 0 : 1);
//        });

        return Result.success(null, list);
    }

    @Override
    public Result<IPage<ExamRecordVO>> getExamRecordPage(Integer pageNum, Integer pageSize,String examName) {
        // 创建page对象
        Page<ExamRecordVO> examPage = new Page<>(pageNum, pageSize);
        // 查询该用户已考试的考试
        examPage = examMapper.getExamRecordPage(examPage, SecurityUtil.getUserId(),examName);
        return Result.success("查询成功", examPage);
    }

    @Override
    public Result<List<ExamRecordDetailVO>> getExamRecordDetail(Integer examId) {
        Function<Integer, List> function = new Function<>() {
            @Override
            public List apply(Integer integer) {
                // 1、题干 2、选项 3、自己的答案 4、正确的答案 5、是否正确 6、试题分析
                List<ExamRecordDetailVO> examRecordDetailVOS = new ArrayList<>();
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
                    ExamRecordDetailVO examRecordDetailVO = new ExamRecordDetailVO();
                    // 设置标题
                    examRecordDetailVO.setImage(temp.getImage());
                    examRecordDetailVO.setTitle(temp.getContent());
                    examRecordDetailVO.setQuType(temp.getQuType());
                    // 设置分析
                    examRecordDetailVO.setAnalyse(temp.getAnalysis());
                    // 查询试题选项
                    LambdaQueryWrapper<Option> optionWrapper = new LambdaQueryWrapper<>();
                    optionWrapper.eq(Option::getQuId, temp.getId());
                    List<Option> options = optionMapper.selectList(optionWrapper);
                    if (temp.getQuType() == 4) {
                        examRecordDetailVO.setOption(null);
                    } else {
                        examRecordDetailVO.setOption(options);
                    }

                    // 查询试题类型
                    LambdaQueryWrapper<Question> QuWrapper = new LambdaQueryWrapper<>();
                    QuWrapper.eq(Question::getId, temp.getId());
                    Question qu = questionMapper.selectOne(QuWrapper);
                    Integer quType = qu.getQuType();
                    // 设置正确答案
                    LambdaQueryWrapper<Option> opWrapper = new LambdaQueryWrapper<>();
                    opWrapper.eq(Option::getQuId, temp.getId());
                    List<Option> opList = optionMapper.selectList(opWrapper);

                    if (temp.getQuType() == 4 && opList.size() > 0) {
                        examRecordDetailVO.setRightOption(opList.get(0).getContent());
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

                        examRecordDetailVO.setRightOption(result);
                    }
                    // 设置是否正确
                    LambdaQueryWrapper<ExamQuAnswer> examQuAnswerWrapper = new LambdaQueryWrapper<>();
                    examQuAnswerWrapper.eq(ExamQuAnswer::getUserId, SecurityUtil.getUserId())
                            .eq(ExamQuAnswer::getExamId, examId)
                            .eq(ExamQuAnswer::getQuestionId, temp.getId());
                    ExamQuAnswer examQuAnswer = examQuAnswerMapper.selectOne(examQuAnswerWrapper);
                    // 如果某题没有作答
                    if (examQuAnswer == null) {
                        examRecordDetailVO.setMyOption(null);
                        examRecordDetailVO.setIsRight(-1);
                        examRecordDetailVOS.add(examRecordDetailVO);
                        continue;
                    }
                    switch (quType) {
                        case 1 -> {
                            // 设置自己的选项
                            LambdaQueryWrapper<Option> optionLambdaQueryWrapper = new LambdaQueryWrapper<>();
                            optionLambdaQueryWrapper.eq(Option::getId, examQuAnswer.getAnswerId());
                            Option op1 = optionMapper.selectOne(optionLambdaQueryWrapper);
                            examRecordDetailVO.setMyOption(Integer.toString(op1.getSort()));
                            // 设置是否正确
                            Option byId = optionService.getById(examQuAnswer.getAnswerId());
                            if (byId.getIsRight() == 1) {
                                examRecordDetailVO.setIsRight(1);
                            } else {
                                examRecordDetailVO.setIsRight(0);
                            }
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
                            examRecordDetailVO.setMyOption(myOption);
                            // 查找正确答案
                            LambdaQueryWrapper<Option> optionWrapper1 = new LambdaQueryWrapper<>();
                            optionWrapper1.eq(Option::getIsRight, 1)
                                    .eq(Option::getQuId, temp.getId());
                            List<Option> examQuAnswers = optionMapper.selectList(optionWrapper);
                            // 判读啊是否正确
                            examRecordDetailVO.setIsRight(1);
                            for (Option temp1 : examQuAnswers) {
                                boolean containsBanana = opIds.contains(temp.getId());
                                if (containsBanana) {
                                    // 只要有一个答案不是正确的则判断为错误
                                    examRecordDetailVO.setIsRight(0);
                                }
                            }
                        }
                        case 3 -> {
                            // 查询自己的的选项
                            LambdaQueryWrapper<Option> optionLambdaQueryWrapper = new LambdaQueryWrapper<>();
                            optionLambdaQueryWrapper.eq(Option::getId, examQuAnswer.getAnswerId());
                            Option op1 = optionMapper.selectOne(optionLambdaQueryWrapper);
                            examRecordDetailVO.setMyOption(Integer.toString(op1.getSort()));
                            // 查询是否正确
                            Option byId = optionService.getById(examQuAnswer.getAnswerId());
                            if (byId.getIsRight() == 1) {
                                examRecordDetailVO.setIsRight(1);
                            } else {
                                examRecordDetailVO.setIsRight(0);
                            }
                        }
                        case 4 -> {
                            examRecordDetailVO.setMyOption(examQuAnswer.getAnswerContent());
                            examRecordDetailVO.setIsRight(-1);
                        }
                        default -> {
                        }
                    }
                    examRecordDetailVOS.add(examRecordDetailVO);

                }
                return examRecordDetailVOS;
            }
        };
        List<ExamRecordDetailVO> result = cacheClient.queryWithPassThrough("cache:record:exam:"+SecurityUtil.getUserId(),
                examId,
                List.class,
                function,
                ExamRecordDetailVO.class,
                10L,
                TimeUnit.MINUTES
        );
        if (result==null){
            return Result.failed("查询失败");
        }

        return Result.success("查询成功", result);
    }

    @Override
    public Result<IPage<ExerciseRecordVO>> getExerciseRecordPage(Integer pageNum, Integer pageSize ,String repoName) {
        // 创建page对象
        Page<Repo> repoPage = new Page<>(pageNum, pageSize);
        // 查询该用户已考试的考试id
        LambdaQueryWrapper<UserExerciseRecord> userExerciseRecordWrapper = new LambdaQueryWrapper<>();
        userExerciseRecordWrapper.eq(UserExerciseRecord::getUserId, SecurityUtil.getUserId());
        List<UserExerciseRecord> userExerciseRecord = userExerciseRecordMapper.selectList(userExerciseRecordWrapper);
        List<Integer> repoIds = userExerciseRecord.stream()
                .map(UserExerciseRecord::getRepoId)
                .collect(Collectors.toList());
        // 查询考试表的考试信息
        LambdaQueryWrapper<Repo> examWrapper = new LambdaQueryWrapper<>();
        examWrapper.in(Repo::getId, repoIds)
                .like(StringUtils.isNotBlank(repoName),Repo::getTitle,repoName);
        Page<Repo> exercisePageResult = repoMapper.selectPage(repoPage, examWrapper);
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
            exerciseRecordDetailVO.setTitle(temp.getContent());
            exerciseRecordDetailVO.setAnalyse(temp.getAnalysis());
            exerciseRecordDetailVO.setQuType(temp.getQuType());
            // 查询试题选项
            LambdaQueryWrapper<Option> optionWrapper = new LambdaQueryWrapper<>();
            optionWrapper.eq(Option::getQuId, temp.getId());
            List<Option> options = optionMapper.selectList(optionWrapper);
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
                case 1 -> {
                    // 设置自己的选项
                    LambdaQueryWrapper<Option> optionLambdaQueryWrapper = new LambdaQueryWrapper<>();
                    optionLambdaQueryWrapper.eq(Option::getId, exerciseRecord.getAnswer());
                    Option op1 = optionMapper.selectOne(optionLambdaQueryWrapper);
                    exerciseRecordDetailVO.setMyOption(Integer.toString(op1.getSort()));
                    // 设置是否正确
                    Option byId = optionService.getById(exerciseRecord.getAnswer());
                    if (byId.getIsRight() == 1) {
                        exerciseRecordDetailVO.setIsRight(1);
                    } else {
                        exerciseRecordDetailVO.setIsRight(0);
                    }
                }
                case 2 -> {
                    // 将回答id解析为列表
                    String answerId = exerciseRecord.getAnswer();
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
                    exerciseRecordDetailVO.setMyOption(myOption);
                    // 查找正确答案
                    LambdaQueryWrapper<Option> optionWrapper1 = new LambdaQueryWrapper<>();
                    optionWrapper1.eq(Option::getIsRight, 1)
                            .eq(Option::getQuId, temp.getId());
                    List<Option> examQuAnswers = optionMapper.selectList(optionWrapper);
                    // 判读啊是否正确
                    exerciseRecordDetailVO.setIsRight(1);
                    for (Option temp1 : examQuAnswers) {
                        boolean containsBanana = opIds.contains(temp.getId());
                        if (containsBanana) {
                            // 只要有一个答案不是正确的则判断为错误
                            exerciseRecordDetailVO.setIsRight(0);
                        }
                    }
                }
                case 3 -> {
                    // 查询自己的的选项
                    LambdaQueryWrapper<Option> optionLambdaQueryWrapper = new LambdaQueryWrapper<>();
                    optionLambdaQueryWrapper.eq(Option::getId, exerciseRecord.getAnswer());
                    Option op1 = optionMapper.selectOne(optionLambdaQueryWrapper);
                    exerciseRecordDetailVO.setMyOption(Integer.toString(op1.getSort()));
                    // 查询是否正确
                    Option byId = optionService.getById(exerciseRecord.getAnswer());
                    if (byId.getIsRight() == 1) {
                        exerciseRecordDetailVO.setIsRight(1);
                    } else {
                        exerciseRecordDetailVO.setIsRight(0);
                    }
                }
                case 4 -> {
                    exerciseRecordDetailVO.setMyOption(null);
                    exerciseRecordDetailVO.setIsRight(-1);
                }
                default -> {
                }
            }
            exerciseRecordDetailVOS.add(exerciseRecordDetailVO);
        }
        return Result.success("查询成功", exerciseRecordDetailVOS);
    }

    @Override
    @Transactional
    public Result<QuestionVO> fillAnswer(ExerciseFillAnswerFrom exerciseFillAnswerFrom) {
        ExerciseRecord exerciseRecord = exerciseConverter.fromToEntity(exerciseFillAnswerFrom);
        //默认用户回答正确
        boolean flag = true;
        exerciseRecord.setIsRight(1);

        //对客观题做题正确与否校验
        if (exerciseFillAnswerFrom.getQuType() != 4) {
            List<Integer> options = Arrays.stream(exerciseRecord.getAnswer().split(","))
                    .map(Integer::parseInt).toList();
            List<Integer> rightOptions = new ArrayList<>();
            optionMapper.selectAllByQuestionId(exerciseRecord.getQuestionId()).forEach(option -> {
                if (option.getIsRight() == 1) {
                    rightOptions.add(option.getId());
                }
            });


            if (options.size() != rightOptions.size()) {
                flag = false;
            } else {
                for (Integer option : options) {
                    if (!rightOptions.contains(option)) {
                        flag = false;
                        exerciseRecord.setIsRight(0);
                        break;
                    }
                }
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
        boolean exercised = Optional.ofNullable(databaseExerciseRecord).isEmpty();
        if (exercised) {
            //未做过该题，新增记录
            exerciseRecordMapper.insert(exerciseRecord);
            //获取该题库填作答记录
            LambdaQueryWrapper<UserExerciseRecord> exerciseRecordWrapper = new LambdaQueryWrapper<UserExerciseRecord>()
                    .eq(UserExerciseRecord::getUserId, SecurityUtil.getUserId())
                    .eq(UserExerciseRecord::getRepoId, exerciseRecord.getRepoId());
            UserExerciseRecord userExerciseRecord = userExerciseRecordMapper.selectOne(exerciseRecordWrapper);

            if (Optional.ofNullable(userExerciseRecord).isEmpty()) {
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
        //主观题响应
        if (exerciseRecord.getQuestionType() == 4) {
            return Result.success(null, questionVO);
        }

        return flag ? Result.success("回答正确", questionVO) : Result.success("回答错误", questionVO);
    }

    @Override
    public Result<QuestionVO> getSingle(Integer id) {
        QuestionVO questionVO = questionMapper.selectDetail(id);
        return Result.success("查询成功", questionVO);
    }

    @Override
    public Result<AnswerInfoVO> getAnswerInfo(Integer repoId, Integer quId) {
        QuestionVO questionVO = questionMapper.selectSingle(quId);
        AnswerInfoVO answerInfoVO = exerciseConverter.quVOToAnswerInfoVO(questionVO);
        LambdaQueryWrapper<ExerciseRecord> exerciseRecordLambdaQueryWrapper = new LambdaQueryWrapper<ExerciseRecord>()
                .eq(ExerciseRecord::getRepoId, repoId)
                .eq(ExerciseRecord::getQuestionId, quId)
                .eq(ExerciseRecord::getUserId, SecurityUtil.getUserId());
        ExerciseRecord exerciseRecord = exerciseRecordMapper.selectOne(exerciseRecordLambdaQueryWrapper);

        answerInfoVO.setAnswerContent(exerciseRecord.getAnswer());
        return exerciseRecord.getIsRight() == 1 ?
                Result.success("回答正确", answerInfoVO) : Result.success("回答错误", answerInfoVO);

    }
}