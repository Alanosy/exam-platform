package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.ExamQuAnswerMapper;
import cn.org.alan.exam.mapper.OptionMapper;
import cn.org.alan.exam.mapper.QuestionMapper;
import cn.org.alan.exam.mapper.UserBookMapper;
import cn.org.alan.exam.model.entity.*;
import cn.org.alan.exam.model.vo.answer.AnswerExamPageVO;
import cn.org.alan.exam.model.vo.userbook.BookOneQuVO;
import cn.org.alan.exam.model.vo.userbook.ReUserExamBookVO;
import cn.org.alan.exam.model.vo.userbook.UserExamBookVO;
import cn.org.alan.exam.model.vo.userbook.UserPageBookVO;
import cn.org.alan.exam.service.IUserBookService;
import cn.org.alan.exam.util.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        return null;
    }

    @Override
    public Result<BookOneQuVO> getBookOne(Integer quId) {
        return null;
    }

    @Override
    public Result<String> addBookAnswer() {
        return null;
    }
}
