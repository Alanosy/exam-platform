package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.UserBook;
import cn.org.alan.exam.model.form.userbook.ReUserBookForm;
import cn.org.alan.exam.model.vo.userbook.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 *  服务类
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface IUserBookService extends IService<UserBook> {
    /**
     * 分页查询错题考试
     * @param pageNum
     * @param pageSize
     * @param examName
     * @return
     */
    Result<IPage<UserPageBookVO>> getPage(Integer pageNum, Integer pageSize, String examName);

    /**
     * 查询某考试的错题
     * @param examId
     * @return
     */
    // Result<List<UserExamBookVO>> getUserExamBook(Integer examId);

    /**
     * 查询错题本错题id列表
     * @param examId
     * @return
     */
    Result<List<ReUserExamBookVO>> getReUserExamBook(Integer examId);

    /**
     * 查询单题
     * @param quId
     * @return
     */
    Result<BookOneQuVO> getBookOne(Integer quId);

    /**
     * 填充答案
     * @return
     */
    Result<AddBookAnswerVO> addBookAnswer(ReUserBookForm reUserBookForm);
}
