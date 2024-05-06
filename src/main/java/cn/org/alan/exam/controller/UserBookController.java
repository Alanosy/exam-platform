package cn.org.alan.exam.controller;


import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.userbook.ReUserBookForm;
import cn.org.alan.exam.model.vo.userbook.*;
import cn.org.alan.exam.service.IUserBookService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 错题本管理
 *
 * @author Alan
 * @since 2024-03-21
 */
@RestController
@RequestMapping("/api/userbooks")
public class UserBookController {

    @Resource
    private IUserBookService userBookService;

    /**
     * 分页查询错题考试
     * @param pageNum
     * @param pageSize
     * @param examName
     * @return
     */
    @GetMapping("/paging")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<IPage<UserPageBookVO>> getPage(@RequestParam(value = "pageNum",required = false, defaultValue = "1") Integer pageNum,
                                                 @RequestParam(value = "pageSize",required = false, defaultValue = "10") Integer pageSize,
                                                 @RequestParam(value = "examName",required = false) String examName){

        return userBookService.getPage(pageNum,pageSize,examName);
    }

    /**
     * 查询错题本错题id列表
     * @param examId
     * @return
     */
    @GetMapping("/question/list/{examId}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<List<ReUserExamBookVO>> getReUserExamBook(@PathVariable("examId") Integer examId){
        return userBookService.getReUserExamBook(examId);
    }

    /**
     * 查询单题
     * @param quId
     * @return
     */
    @GetMapping("/question/single/{quId}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<BookOneQuVO> getBookOne(@PathVariable("quId") Integer quId){
        return userBookService.getBookOne(quId);
    }

    /**
     * 填充答案
     * @param reUserBookForm
     * @return
     */
    @PostMapping("/full-book")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<AddBookAnswerVO> addBookAnswer(@RequestBody ReUserBookForm reUserBookForm){
        return userBookService.addBookAnswer(reUserBookForm);
    }
}
