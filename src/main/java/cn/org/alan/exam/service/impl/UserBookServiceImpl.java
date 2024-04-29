package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.UserBookConverter;
import cn.org.alan.exam.mapper.ExamQuAnswerMapper;
import cn.org.alan.exam.mapper.OptionMapper;
import cn.org.alan.exam.mapper.QuestionMapper;
import cn.org.alan.exam.mapper.UserBookMapper;
import cn.org.alan.exam.model.entity.*;
import cn.org.alan.exam.model.form.userbook.ReUserBookForm;
import cn.org.alan.exam.model.vo.userbook.*;
import cn.org.alan.exam.service.IQuestionService;
import cn.org.alan.exam.service.IUserBookService;
import cn.org.alan.exam.util.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Service
public class UserBookServiceImpl extends ServiceImpl<UserBookMapper, UserBook> implements IUserBookService {
    @Resource
    private UserBookMapper userBookMapper;
    @Resource
    private QuestionMapper questionMapper;
    @Resource
    private OptionMapper optionMapper;
    @Resource
    private ExamQuAnswerMapper examQuAnswerMapper;
    @Resource
    private UserBookConverter userBookConverter;
    @Resource
    private IQuestionService questionService;
    @Override
    public Result<IPage<UserPageBookVO>> getPage(Integer pageNum, Integer pageSize, String examName) {
        Page<UserPageBookVO> page = new Page<>(pageNum, pageSize);
        Page<UserPageBookVO> userPageBookVOPage = userBookMapper.selectPageVo(page, examName, SecurityUtil.getUserId());
        return Result.success("查询成功",userPageBookVOPage);
    }

    @Override
    public Result<List<UserExamBookVO>> getUserExamBook(Integer examId) {
        List<UserExamBookVO> userExamBookVOS = new ArrayList<>();

        LambdaQueryWrapper<UserBook> userBookLambdaQuery = new LambdaQueryWrapper<>();
        userBookLambdaQuery.eq(UserBook::getUserId,SecurityUtil.getUserId())
                .eq(UserBook::getExamId,examId);
        List<UserBook> userBooks = userBookMapper.selectList(userBookLambdaQuery);
        for(UserBook temp: userBooks){
            UserExamBookVO userExamBookVO = new UserExamBookVO();
            // 题干 选项 所填答案 正确答案 分析
            // 设置题干
            LambdaQueryWrapper<Question> questionLambdaQueryWrapper = new LambdaQueryWrapper<>();
            questionLambdaQueryWrapper.eq(Question::getId,temp.getQuId());
            Question question = questionMapper.selectOne(questionLambdaQueryWrapper);
            userExamBookVO.setContent(question.getContent());
            // 设置分析
            userExamBookVO.setAnalyse(question.getAnalysis());
            // 设置选项
            LambdaQueryWrapper<Option> optionLambdaQueryWrapper = new LambdaQueryWrapper<Option>();
            optionLambdaQueryWrapper.eq(Option::getQuId,temp.getQuId());
            List<Option> options = optionMapper.selectList(optionLambdaQueryWrapper);
            userExamBookVO.setAnswerList(options);
            // 设置用户的回答 未完成
            LambdaQueryWrapper<ExamQuAnswer> examQuAnswerLambdaQueryWrapper = new LambdaQueryWrapper<>();
            examQuAnswerLambdaQueryWrapper.eq(ExamQuAnswer::getExamId,examId)
                    .eq(ExamQuAnswer::getUserId,SecurityUtil.getUserId())
                    .eq(ExamQuAnswer::getQuestionId,temp.getQuId());
            ExamQuAnswer examQuAnswer = examQuAnswerMapper.selectOne(examQuAnswerLambdaQueryWrapper);
            userExamBookVO.setReply(examQuAnswer.getAnswerId());
            // 设置正确答案 未完成

            userExamBookVOS.add(userExamBookVO);
        }
        return Result.success("查询成功",userExamBookVOS);
    }

    @Override
    public Result<List<ReUserExamBookVO>> getReUserExamBook(Integer examId) {
        // 根据考试id查询考试试题表
        LambdaQueryWrapper<UserBook> userBookLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userBookLambdaQueryWrapper.eq(UserBook::getExamId, examId)
                .eq(UserBook::getUserId,SecurityUtil.getUserId());
        List<UserBook> userBook = userBookMapper.selectList(userBookLambdaQueryWrapper);
        // 实体转换
        List<ReUserExamBookVO> reUserExamBookVOS = userBookConverter.listEntityToVo(userBook);
        return Result.success("查询成功", reUserExamBookVOS);
    }

    @Override
    public Result<BookOneQuVO> getBookOne(Integer quId) {
        BookOneQuVO bookOneQuVO = new BookOneQuVO();
        // 问题
        Question quById = questionService.getById(quId);
        // 基本信息
        bookOneQuVO.setImage(quById.getImage());
        bookOneQuVO.setContent(quById.getContent());
        // 答案列表
        LambdaQueryWrapper<Option> optionLambdaQuery = new LambdaQueryWrapper<>();
        optionLambdaQuery.eq(Option::getQuId, quId);
        List<Option> list = optionMapper.selectList(optionLambdaQuery);
        bookOneQuVO.setAnswerList(list);
        return Result.success("获取成功", bookOneQuVO);
    }

    @Override
    public Result<AddBookAnswerVO> addBookAnswer(ReUserBookForm reUserBookForm) {

        AddBookAnswerVO addBookAnswerVO = new AddBookAnswerVO();
        // 未作答
        if (CollectionUtils.isEmpty(Collections.singleton(reUserBookForm.getAnswer()))
                && StringUtils.isBlank(reUserBookForm.getAnswer())) {
            return null;
        }
        // 查找答案列表
        LambdaQueryWrapper<Option> optionLambdaQueryWrapper = new LambdaQueryWrapper<>();
        optionLambdaQueryWrapper.eq(Option::getQuId,reUserBookForm.getQuId());
        List<Option> list = optionMapper.selectList(optionLambdaQueryWrapper);

        LambdaQueryWrapper<Question> questionLambdaQueryWrapper = new LambdaQueryWrapper<>();
        questionLambdaQueryWrapper.eq(Question::getId,reUserBookForm.getQuId());
        Question question = questionMapper.selectOne(questionLambdaQueryWrapper);
        if(question.getQuType()==0){
            // 单选题
            LambdaQueryWrapper<Option> optionLambdaQueryWrapper1 = new LambdaQueryWrapper<>();
            optionLambdaQueryWrapper1.eq(Option::getQuId,reUserBookForm.getQuId());
            Option option = optionMapper.selectOne(optionLambdaQueryWrapper1);
            if(option.getIsRight()==1){
                addBookAnswerVO.setCorrect(1);
            }
        }else if(question.getQuType()==1){
            //多选题
            List<Integer> examIds = Arrays.stream(reUserBookForm.getAnswer().split(","))
                    .map(Integer::parseInt)
                    .toList();
        }else if(question.getQuType()==2){
            // 判断题
        } else if (question.getQuType()==3) {
            // 简答题
        }
        addBookAnswerVO.setAnalysis(question.getAnalysis());
        addBookAnswerVO.setRightAnswers(null);

        return Result.success("success",addBookAnswerVO);
    }
}
