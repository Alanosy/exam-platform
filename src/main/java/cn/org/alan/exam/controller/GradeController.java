package cn.org.alan.exam.controller;


import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.Grade;
import cn.org.alan.exam.model.form.GradeForm;
import cn.org.alan.exam.model.vo.GradeVO;
import cn.org.alan.exam.service.IGradeService;
import cn.org.alan.exam.service.IRepoService;
import cn.org.alan.exam.service.impl.GradeServiceImpl;
import cn.org.alan.exam.util.JwtUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.server.PathParam;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.sound.midi.Patch;
import java.util.List;

/**
 * 班级管理
 *
 * @author Alan
 * @since 2024-03-21
 */
@RestController
@RequestMapping("/api/grades")
public class GradeController {
    @Resource
    private IGradeService gradeService;
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private HttpServletRequest request;

    /**
     * 分页查询班级列表
     *
     * @param pageNum
     * @param pageSize
     * @param gradeName
     * @return
     */
    @GetMapping("/paging")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<IPage<GradeVO>> getGrade(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                           @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                           @RequestParam(value = "gradeName", required = false) String gradeName) {
        return gradeService.getPaging(jwtUtil.getUserId(request), pageNum, pageSize, gradeName);
    }

    /**
     * 创建班级
     *
     * @param gradeForm
     * @return
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> addGrade(@RequestBody GradeForm gradeForm) {
        return gradeService.addGrade(jwtUtil.getUserId(request), gradeForm);
    }

    /**
     * 修改班级名称
     *
     * @param id
     * @param gradeForm
     * @return
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> updateGrade(@PathVariable("id") String id, @RequestBody GradeForm gradeForm) {
        return gradeService.updateGrade(id, gradeForm);
    }

    /**
     * 删除班级
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> deleteGrade(@PathVariable("id") Integer id) {
        return gradeService.deleteGrade(id);
    }

    /**
     * 批量退出班级
     *
     * @param ids
     * @return
     */
    @PutMapping("/remove/{ids}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<String> removeUserGrade(@PathVariable("ids") String ids) {
        return gradeService.removeUserGrade(ids);
    }

    /**
     * 查询所有班级
     *
     * @return
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<List<GradeVO>> getGradeAll() {
        return gradeService.getGradeAll(jwtUtil.getUserId(request));
    }

    /**
     * 用户加入班级
     *
     * @param code
     * @return
     */
    @PatchMapping("/add/{code}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<String> userAddGrade(@PathVariable("code") String code) {
        return gradeService.userAddGrade(jwtUtil.getUserId(request), code);
    }

}
