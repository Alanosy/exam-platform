package cn.org.alan.exam.controller;


import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.userbook.ReUserBookForm;
import cn.org.alan.exam.model.vo.userbook.BookOneQuVO;
import cn.org.alan.exam.model.vo.userbook.ReUserExamBookVO;
import cn.org.alan.exam.model.vo.userbook.UserExamBookVO;
import cn.org.alan.exam.model.vo.userbook.UserPageBookVO;
import cn.org.alan.exam.service.IUserBookService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *  错题本管理
 *
 * @author Alan
 * @since 2024-03-21
 */
@RestController
@RequestMapping("/api/userbook")
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
    public Result<IPage<UserPageBookVO>> getPage(@RequestParam(value = "pageNum",required = false, defaultValue = "1") Integer pageNum,
                                                 @RequestParam(value = "pageSize",required = false, defaultValue = "10") Integer pageSize,
                                                 @RequestParam(value = "examName",required = false) String examName){

        return userBookService.getPage(pageNum,pageSize,examName);
    }

    /**
     * 查询某考试的错题
     * @param examId
     * @return
     */
    @GetMapping("/exam/{examId}")
    public Result<List<UserExamBookVO>> getUserExamBook(@PathVariable("examId") Integer examId){
        return userBookService.getUserExamBook(examId);
    }

    // 重刷某考试的错题

    /**
     * 查询错题本错题id列表
     * @param examId
     * @return
     */
    @GetMapping("/{examId}")
    public Result<List<ReUserExamBookVO>> getReUserExamBook(@PathVariable("examId") Integer examId){
        return userBookService.getReUserExamBook(examId);
    }

    /**
     * 查询单题
     * @param quId
     * @return
     */
    @GetMapping("/{quId}")
    public Result<BookOneQuVO> getBookOne(@PathVariable("quId") Integer quId){
        return userBookService.getBookOne(quId);
    }

    /**
     * 填充答案
     * @param reUserBookForm
     * @return
     */
    @PostMapping("/full-book")
    public Result<String> addBookAnswer(@RequestBody ReUserBookForm reUserBookForm){
        return userBookService.addBookAnswer();
    }
}
