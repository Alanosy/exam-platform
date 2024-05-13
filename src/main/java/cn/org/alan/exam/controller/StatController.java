package cn.org.alan.exam.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.vo.stat.AllStatsVO;
import cn.org.alan.exam.model.vo.stat.GradeExamVO;
import cn.org.alan.exam.model.vo.stat.GradeStudentVO;
import cn.org.alan.exam.service.IStatService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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
    private IStatService statService;
    /**
     * 各班级人数统计
     * @return
     */
    @GetMapping("/student")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<List<GradeStudentVO>> getStudentGradeCount() {
        return statService.getStudentGradeCount();
    }

    /**
     * 各班试卷统计
     * @return
     */
    @GetMapping("/exam")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<List<GradeExamVO>> getExamGradeCount() {
        return statService.getExamGradeCount();
    }

    /**
     * 统计所有班级、试卷、试题数量
     *
     * @return 统计结果
     */
    @GetMapping("/allCounts")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<AllStatsVO> getAllCount(){
        return statService.getAllCount();
    }












}
