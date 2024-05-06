package cn.org.alan.exam.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.Exam;
import cn.org.alan.exam.model.form.count.ClassCountResult;
import cn.org.alan.exam.service.IExamGradeService;
import cn.org.alan.exam.service.IGradeService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 统计管理
 *
 * @Author Alan
 * @Version
 * @Date 2024/3/25 11:22 AM
 */
@RestController
@RequestMapping("/stat")
public class StatController {

    @Resource
    private IGradeService gradeService;

    @Resource
    private IExamGradeService examGradeService;
    /**
     * 各班级人数统计
     * @return
     */
    @GetMapping("/total/grade/count{roleId}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<List<ClassCountResult>> getClassCountByRoleId(@PathVariable int roleId) {
        return Result.success(null,gradeService.countStudentsByRoleId(roleId));
    }

    /**
     * 各班试卷统计
     * @return
     */
    @GetMapping("teacher/{roleid}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<List<Exam>> getExamGradeCount(@PathVariable("roleId") int roleId) {
        return Result.success(null,examGradeService.getExamGradeCount(roleId));
    }

    /**
     * 统计所有班级、试卷、试题数量
     * @return 统计结果
     */
    @GetMapping("/allCounts")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<ClassCountResult> getAllCounts(){
        return gradeService.getAllCounts();
    }












}
