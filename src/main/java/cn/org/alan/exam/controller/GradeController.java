package cn.org.alan.exam.controller;


import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.GradeForm;
import cn.org.alan.exam.service.IGradeService;
import cn.org.alan.exam.util.JwtUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 班级管理
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@RestController
@RequestMapping("/api/grades")
public class GradeController {
    @Autowired
    private IGradeService gradeService;


    @GetMapping("/paging")
    public Result getGrade(@Param("pageNum") Integer pageNum,
                           @Param("pageSize") Integer pageSize,
                           @Param("gradeName") String gradeName) {
        gradeService.getPaging(pageNum, pageSize, gradeName);
        return Result.success();
    }

    @PostMapping("/add")
    public Result<String> addGrade(@RequestBody GradeForm gradeForm) {
        gradeService.addGrade(gradeForm);

        return Result.success();
    }

    @PutMapping("/update/{id}")
    public Result<String> updateGrade(@PathVariable("id") Integer id, @RequestBody GradeForm gradeForm) {
        gradeService.updateGrade(id, gradeForm);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> deleteGrade(@PathVariable("id") Integer id) {
        gradeService.deleteGrade(id);
        return Result.success();
    }

}
