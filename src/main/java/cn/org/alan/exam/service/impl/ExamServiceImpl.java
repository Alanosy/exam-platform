package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.ExamConverter;
import cn.org.alan.exam.mapper.*;
import cn.org.alan.exam.model.entity.*;
import cn.org.alan.exam.model.form.exam.ExamAddForm;
import cn.org.alan.exam.model.form.exam.ExamForm;
import cn.org.alan.exam.model.vo.*;
import cn.org.alan.exam.model.vo.exam.ExamQuAnswerForm;
import cn.org.alan.exam.model.vo.exam.PaperQuAnswerExtVO;
import cn.org.alan.exam.model.vo.exam.PaperQuDetailVO;
import cn.org.alan.exam.service.IExamQuAnswerService;
import cn.org.alan.exam.service.IExamQuestionService;
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
import org.springframework.util.CollectionUtils;

import java.util.*;
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
    private IExamQuestionService examQuestionService;
    @Resource
    private ExamRepoMapper examRepoMapper;
    @Resource
    private ExamQuestionMapper examQuestionMapper;

    @Override
    public Result<String> createExam(ExamAddForm examAddForm) {
        // 添加考试信息到考试表
        int rowsAffected = examMapper.createExam(examAddForm);
        // 添加考试班级
        String gradeids = examAddForm.getGradeids();
        List<Integer> gradeIds = Arrays.stream(gradeids.split(","))
                .map(Integer::parseInt)
                .toList();
        ExamGrade examGrade = new ExamGrade();
        examGrade.setExamId(examAddForm.getId());
        for (Integer gradeId : gradeIds) {
            examGrade.setGradeId(gradeId);
            examGradeMapper.insert(examGrade);
        }
        // 设置考试的题库
        Integer repoId = examAddForm.getRepoId();
        ExamRepo examRepo = new ExamRepo();
        examRepo.setExamId(examAddForm.getId());
        examRepo.setRepoId(repoId);
        examRepoMapper.insert(examRepo);
        // 开始抽题
        Map<Integer, Integer> quTypeToCount = new HashMap<>();
        quTypeToCount.put(1, examAddForm.getRadioCount()); // 单选题数量
        quTypeToCount.put(2, examAddForm.getMultiCount()); // 多选题
        quTypeToCount.put(3, examAddForm.getJudgeCount()); // 判断题
        quTypeToCount.put(4, examAddForm.getSaqCount()); // 简答题
        Map<Integer, Integer> quTypeToScore = new HashMap<>();
        quTypeToScore.put(1, examAddForm.getRadioScore()); // 单选题分数
        quTypeToScore.put(2, examAddForm.getMultiScore()); // 多选题
        quTypeToScore.put(3, examAddForm.getJudgeScore()); // 判断题
        quTypeToScore.put(4, examAddForm.getSaqScorre()); // 简答题
        for (Map.Entry<Integer, Integer> entry : quTypeToCount.entrySet()) {
            Integer quType = entry.getKey();
            Integer count = entry.getValue();
            ExamQuestion examQuestion = new ExamQuestion();
            examQuestion.setExamId(examAddForm.getId());
            examQuestion.setType(quType);
            examQuestion.setScore(quTypeToScore.get(quType));

            LambdaQueryWrapper<Question> typeQueryWrapper = new LambdaQueryWrapper<>();
            typeQueryWrapper.select(Question::getId)
                    .eq(Question::getQuType, quType)
                    .eq(Question::getRepoId, examAddForm.getRepoId());

            List<Question> questionsByType = questionMapper.selectList(typeQueryWrapper);

            if (questionsByType.size() < count) {
                System.out.println("题库中类型为" + quType + "的题目数量不足" + count + "个！");
                continue;
            }

            List<Integer> typeQuestionIds = questionsByType.stream().map(Question::getId).collect(Collectors.toList());
            Collections.shuffle(typeQuestionIds);

            List<Integer> sampledIds = typeQuestionIds.subList(0, count);
            for (int i = 0; i < sampledIds.toArray().length; i++) {
                examQuestion.setQuestionId(sampledIds.get(i));
                examQuestion.setSort(i);
                examQuestionMapper.insert(examQuestion);
            }
        }

        // 返回值，未写完 4月7日 加事务......
        if (rowsAffected == 0) {
            return Result.failed("创建失败");
        }
        return Result.success("创建成功");
    }

    @Override
    public Result<String> updateExam(ExamForm examForm, Integer id) {

        LambdaUpdateWrapper<Exam> examLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        examLambdaUpdateWrapper.eq(Exam::getId, id)
                .eq(Exam::getUserId, SecurityUtil.getUserId());
        Exam exam = examConverter.formToEntity(examForm);
        int rowsAffected = examMapper.update(exam, examLambdaUpdateWrapper);
        if (rowsAffected == 0) {
            return Result.failed("修改失败");
        }
        return Result.success("修改成功");
    }

    @Override
    public Result<String> deleteExam(String ids) {
        List<Integer> examIds = Arrays.stream(ids.split(","))
                .map(Integer::parseInt)
                .toList();
        int rowsAffected = examMapper.deleteExam(examIds);
        if (rowsAffected == 0) {
            return Result.failed("删除失败");
        }
        return Result.success("删除成功");
    }

    @Override
    public Result<IPage<ExamVO>> getPagingExam(Integer pageNum, Integer pageSize, String title) {
        Page<Exam> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Exam> examQueryWrapper = new LambdaQueryWrapper<>();
        examQueryWrapper.like(StringUtils.isNotBlank(title), Exam::getTitle, title)
                .eq(Exam::getUserId, SecurityUtil.getUserId());
        Page<Exam> examPage = examMapper.selectPage(page, examQueryWrapper);
        return Result.success("查询成功", examConverter.pageEntityToVo(examPage));
    }

    @Override
    public Result<ExamDetailRespVO> getQuestionList(String id) {
        LambdaQueryWrapper<ExamQuestion> examQuestionLambdaQueryWrapper = new LambdaQueryWrapper<>();
        examQuestionLambdaQueryWrapper.eq(ExamQuestion::getExamId, id);
        examQuestionMapper.selectList(examQuestionLambdaQueryWrapper);
        // 未写完 4月7......
        return null;
    }

    @Override
    public Result<PaperQuDetailVO> getQuestionSingle(String examId, String questionId) {
        PaperQuDetailVO paperQuDetailVO = new PaperQuDetailVO();
        // 问题
        Question quById = questionService.getById(questionId);
        // 基本信息
        // QueryWrapper<ExamQuestion> wrapper = new QueryWrapper<>();
        // wrapper.lambda().eq(ExamQuestion::getExamId, examId)
        //         .eq(ExamQuestion::getQuestionId, questionId);
        // ExamQuestion one = examQuestionService.getOne(wrapper, false);
        paperQuDetailVO.setImage(quById.getImage());
        paperQuDetailVO.setContent(quById.getContent());
        // 答案列表
        List<PaperQuAnswerExtVO> list = examQuAnswerService.listForExam(examId, questionId);
        paperQuDetailVO.setAnswerList(list);
        return Result.success("获取成功",paperQuDetailVO);
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
        if(CollectionUtils.isEmpty(Collections.singleton(examQuAnswerForm.getAnswer()))
                && StringUtils.isBlank(examQuAnswerForm.getAnswer())){
            return null;
        }

        //查找答案列表
        List<ExamQuAnswer> list = examQuAnswerService.listForFill(examQuAnswerForm.getExamId(), examQuAnswerForm.getQuestionId());

        //是否正确
        boolean right = true;
        //更新正确答案
        for (ExamQuAnswer item : list) {

            if (examQuAnswerForm.getAnswers().contains(item.getId())) {
                item.setCheckout(1);
            } else {
                item.setCheckout(0);
            }

            //有一个对不上就是错的
            if (item.getIsRight()!=null && !item.getIsRight().equals(item.getCheckout())) {
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
    public Result<ExamVO> getGradeExamList(String id) {
        return null;
    }
}
