package cn.org.alan.exam.controller;


import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.service.IExamService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 考试管理
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@RestController
@RequestMapping("/exams")
public class ExamController {

    @Resource
    private IExamService iExamService;

//    public Result<String> addExam(){
//
//    }

}
