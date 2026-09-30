package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.UserBookConverter;
import cn.org.alan.exam.mapper.ExamQuAnswerMapper;
import cn.org.alan.exam.mapper.OptionMapper;
import cn.org.alan.exam.mapper.QuestionMapper;
import cn.org.alan.exam.mapper.UserBookMapper;
import cn.org.alan.exam.model.entity.*;
import cn.org.alan.exam.model.form.userbook.ReUserBookForm;
import cn.org.alan.exam.model.vo.userbook.*;
import cn.org.alan.exam.service.IOptionService;
import cn.org.alan.exam.service.IQuestionService;
import cn.org.alan.exam.service.IUserBookService;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 错题本服务实现类
 *
 * @author Alan
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
    private UserBookConverter userBookConverter;
    @Resource
    private IQuestionService questionService;
    @Resource
    private IOptionService optionService;

    @Override
    public Result<IPage<UserPageBookVO>> getPage(Integer pageNum, Integer pageSize, String examName) {
        Page<UserPageBookVO> page = new Page<>(pageNum, pageSize);
        // 获取userId
        Integer userId = SecurityUtil.getUserId();
        // 开始查询有错题的考试
        Page<UserPageBookVO> userPageBookVOPage = userBookMapper.selectPageVo(page, examName, userId);
        return Result.success("查询成功", userPageBookVOPage);
    }

    @Override
    public Result<List<ReUserExamBookVO>> getReUserExamBook(Integer examId) {
        // 根据考试id查询考试试题表
        Integer userId = SecurityUtil.getUserId();
        LambdaQueryWrapper<UserBook> userBookLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userBookLambdaQueryWrapper.eq(UserBook::getExamId, examId)
                .eq(UserBook::getUserId, userId);
        List<UserBook> userBook = userBookMapper.selectList(userBookLambdaQueryWrapper);
        // 实体转换
        List<ReUserExamBookVO> reUserExamBookVOS = userBookConverter.listEntityToVo(userBook);
        return Result.success("查询错题本错题id列表成功", reUserExamBookVOS);
    }

    @Override
    public Result<BookOneQuVO> getBookOne(Integer quId) {
        BookOneQuVO bookOneQuVO = new BookOneQuVO();
        // 问题
        Question quById = questionService.getById(quId);
        if (quById == null) {
            throw new ServiceRuntimeException("该题不存在");
        }
        // 基本信息
        bookOneQuVO.setImage(quById.getImage());
        bookOneQuVO.setContent(quById.getContent());
        bookOneQuVO.setQuType(quById.getQuType());
        // 答案列表
        LambdaQueryWrapper<Option> optionLambdaQuery = new LambdaQueryWrapper<>();
        optionLambdaQuery.eq(Option::getQuId, quId);
        List<Option> list = optionMapper.selectList(optionLambdaQuery);
        bookOneQuVO.setAnswerList(list);
        return Result.success("获取成功", bookOneQuVO);
    }

    @Override
    public Result<AddBookAnswerVO> addBookAnswer(ReUserBookForm reUserBookForm) {
        Integer userId = SecurityUtil.getUserId();
        // 创建返回视图
        AddBookAnswerVO addBookAnswerVO = new AddBookAnswerVO();
        // 未作答
        if (StringUtils.isBlank(reUserBookForm.getAnswer())) {
            return Result.success("未作答");
        }
        // 查询试题类型
        LambdaQueryWrapper<Question> QuWrapper = new LambdaQueryWrapper<>();
        QuWrapper.eq(Question::getId, reUserBookForm.getQuId());
        Question qu = questionMapper.selectOne(QuWrapper);
        Integer quType = qu.getQuType();
        // 设置试题分析
        addBookAnswerVO.setAnalysis(qu.getAnalysis());
        // 设置正确答案
        LambdaQueryWrapper<Option> opWrapper = new LambdaQueryWrapper<>();
        opWrapper.eq(Option::getQuId, reUserBookForm.getQuId());
        List<Option> options = optionMapper.selectList(opWrapper);
        String current = "";
        ArrayList<Integer> strings = new ArrayList<>();
        for (Option temp : options) {
            if (temp.getIsRight() == 1) {
                strings.add(temp.getSort());
            }
        }
        List<String> stringList = strings.stream().map(String::valueOf).collect(Collectors.toList());
        String result = String.join(",", stringList);
        if (quType == 4) {
            addBookAnswerVO.setRightAnswers(options.get(0).getContent());
        } else {
            addBookAnswerVO.setRightAnswers(result);
        }

        // 判断是否正确并移除正确试题
        switch (quType) {
            case 1:
                Option byId1 = optionService.getById(reUserBookForm.getAnswer());
                if (byId1.getIsRight() == 1) {
                    LambdaQueryWrapper<UserBook> userBookLambdaQueryWrapper1 = new LambdaQueryWrapper<>();
                    userBookLambdaQueryWrapper1.eq(UserBook::getUserId, userId)
                            .eq(UserBook::getExamId, reUserBookForm.getExamId())
                            .eq(UserBook::getQuId, reUserBookForm.getQuId());
                    int row = userBookMapper.delete(userBookLambdaQueryWrapper1);
                    addBookAnswerVO.setCorrect(1);
                    return Result.success("回答正确，已移出错题本", addBookAnswerVO);
                } else {
                    addBookAnswerVO.setCorrect(0);
                    return Result.success("回答错误", addBookAnswerVO);
                }
            case 2:
                // 查找正确答案
                LambdaQueryWrapper<Option> optionWrapper = new LambdaQueryWrapper<>();
                optionWrapper.eq(Option::getIsRight, 1)
                        .eq(Option::getQuId, reUserBookForm.getQuId());
                List<Option> examQuAnswers = optionMapper.selectList(optionWrapper);
                // 解析用户作答
                List<Integer> quIds = Arrays.stream(reUserBookForm.getAnswer().split(","))
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());
                // 判断是否正确
                boolean isWrong = false;
                for (Option temp : examQuAnswers) {
                    if (!quIds.contains(temp.getId())) {
                        isWrong = true;
                        break;
                    }
                }
                if (isWrong) {
                    addBookAnswerVO.setCorrect(0);
                    return Result.success("回答错误",addBookAnswerVO);
                }
                LambdaQueryWrapper<UserBook> userBookWrapper = new LambdaQueryWrapper<>();
                userBookWrapper.eq(UserBook::getUserId, userId)
                        .eq(UserBook::getExamId, reUserBookForm.getExamId())
                        .eq(UserBook::getQuId, reUserBookForm.getQuId());
                userBookMapper.delete(userBookWrapper);
                addBookAnswerVO.setCorrect(1);
                return Result.success("回答正确",addBookAnswerVO);
            case 3:
                Option byId3 = optionService.getById(reUserBookForm.getAnswer());
                if (byId3.getIsRight() == 1) {
                    LambdaQueryWrapper<UserBook> userBookLambdaQueryWrapper3 = new LambdaQueryWrapper<>();
                    userBookLambdaQueryWrapper3.eq(UserBook::getUserId, userId)
                            .eq(UserBook::getExamId, reUserBookForm.getExamId())
                            .eq(UserBook::getQuId, reUserBookForm.getQuId());
                    userBookMapper.delete(userBookLambdaQueryWrapper3);
                    addBookAnswerVO.setCorrect(1);
                    return Result.success("回答正确，已移除错题本", addBookAnswerVO);
                } else {
                    addBookAnswerVO.setCorrect(0);
                    return Result.success("回答错误", addBookAnswerVO);
                }
            case 4:
                if ("1".equals(reUserBookForm.getAnswer())) {
                    LambdaQueryWrapper<UserBook> userBookLambdaQueryWrapper4 = new LambdaQueryWrapper<>();
                    userBookLambdaQueryWrapper4.eq(UserBook::getUserId, userId)
                            .eq(UserBook::getExamId, reUserBookForm.getExamId())
                            .eq(UserBook::getQuId, reUserBookForm.getQuId());
                    userBookMapper.delete(userBookLambdaQueryWrapper4);
                    return Result.success("回答正确，已移除错题本", addBookAnswerVO);
                }
                addBookAnswerVO.setCorrect(0);
                return Result.success("回答错误", addBookAnswerVO);
            default:
                throw new ServiceRuntimeException("填充答案请求错误");
        }
    }
}
