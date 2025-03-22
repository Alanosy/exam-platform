package cn.org.alan.exam.controller;


import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.grade.GradeForm;
import cn.org.alan.exam.model.vo.grade.GradeVO;
import cn.org.alan.exam.service.IGradeService;
import com.baomidou.mybatisplus.core.metadata.IPage;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 班级管理
 *
 * @author Alan
 * @since 2024-03-21
 */
@Api(tags = "班级管理相关接口")
@RestController
@RequestMapping("/api/grades")
public class GradeController {

    @Resource
    private IGradeService gradeService;

    /**
     * 分页查询班级
     *
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @param gradeName 班级名称
     * @return
     */
    @ApiOperation("分页查询班级")
    @GetMapping("/paging")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<IPage<GradeVO>> getGrade(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                           @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                           @RequestParam(value = "gradeName", required = false) String gradeName) {
        return gradeService.getPaging(pageNum, pageSize, gradeName);
    }

    /**
     * 新增班级
     *
     * @param gradeForm
     * @return
     */
    @ApiOperation("新增班级")
    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<String> addGrade(@Validated @RequestBody GradeForm gradeForm) {
        return gradeService.addGrade(gradeForm);
    }

    /**
     * 修改班级
     *
     * @param id
     * @param gradeForm
     * @return
     */
    @ApiOperation("修改班级")
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<String> updateGrade(@PathVariable("id") @NotNull Integer id, @Validated @RequestBody GradeForm gradeForm) {
        return gradeService.updateGrade(id, gradeForm);
    }

    /**
     * 删除班级
     *
     * @param id 班级ID
     * @return
     */
    @ApiOperation("删除班级")
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('role_admin')")
    public Result<String> deleteGrade(@PathVariable("id") @NotNull Integer id) {
        return gradeService.deleteGrade(id);
    }

    /**
     * 退出班级
     *
     * @param ids 班级ID
     * @return
     */
    @ApiOperation("退出班级")
    @PatchMapping("/remove/{ids}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<String> removeUserGrade(@PathVariable("ids") @NotBlank String ids) {
        return gradeService.removeUserGrade(ids);
    }

    /**
     * 获取所有班级列表
     *
     * @return
     */
    @ApiOperation("获取所有班级列表")
    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<List<GradeVO>> getAllGrade() {
        return gradeService.getAllGrade();
    }

    /**
     * 老师加入班级
     *
     * @param code 班级代码
     * @return
     */
    @ApiOperation("老师加入班级")
    @GetMapping("/teacher/join")
    @PreAuthorize("hasAnyAuthority('role_teacher')")
    public Result teacherJoinClass(@RequestParam("code") String code) {
        return gradeService.teacherJoinClass(code);
    }

    /**
     * 老师退出班级
     *
     * @param gradeId 班级ID
     * @return
     */
    @ApiOperation("老师退出班级")
    @DeleteMapping("/teacher/exit/{gradeId}")
    @PreAuthorize("hasAnyAuthority('role_teacher')")
    public Result teacherExitClass(@PathVariable("gradeId") String gradeId) {
        return gradeService.teacherExitClass(gradeId);
    }

    /**
     * 学生退出班级
     *
     * @return
     */
    @ApiOperation("学生退出班级")
    @PutMapping("/user/exit")
    @PreAuthorize("hasAnyAuthority('role_student')")
    public Result userExitGrade() {
        return gradeService.userExitGrade();
    }
}
