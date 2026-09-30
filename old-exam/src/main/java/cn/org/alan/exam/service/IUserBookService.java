package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.UserBook;
import cn.org.alan.exam.model.form.userbook.ReUserBookForm;
import cn.org.alan.exam.model.vo.userbook.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 错题本服务类
 *
 * @author Alan
 * @since 2024-03-21
 */
public interface IUserBookService extends IService<UserBook> {
    /**
     * 学生错题本分页查询
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param examName 考试标题
     * @return
     */
    Result<IPage<UserPageBookVO>> getPage(Integer pageNum, Integer pageSize, String examName);

    /**
     * 查询错题本错题id列表
     *
     * @param examId 考试ID
     * @return
     */
    Result<List<ReUserExamBookVO>> getReUserExamBook(Integer examId);

    /**
     * 查询单题
     *
     * @param quId 试题ID
     * @return
     */
    Result<BookOneQuVO> getBookOne(Integer quId);

    /**
     * 填充答案
     *
     * @return
     */
    Result<AddBookAnswerVO> addBookAnswer(ReUserBookForm reUserBookForm);
}
