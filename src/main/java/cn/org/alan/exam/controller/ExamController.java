package cn.org.alan.exam.controller;


import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.exam.ExamAddForm;
import cn.org.alan.exam.model.form.exam.ExamUpdateForm;
import cn.org.alan.exam.model.vo.exam.*;
import cn.org.alan.exam.service.IExamService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Pattern;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 考试管理
 *
 * @author Alan
 * @since 2024-03-21
 */
@RestController
@RequestMapping("/api/exams")
public class ExamController {

    @Resource
    private IExamService examService;

    /**
     * 创建考试
     * @param examAddForm
     * @return
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> createExam(@Validated @RequestBody ExamAddForm examAddForm) {
        return examService.createExam(examAddForm);
    }

    /**
     * 开始考试
     * @param examId
     * @return
     */
    @GetMapping("/start")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<String> startExam(@RequestParam("exam_id") Integer examId) {
        return examService.startExam(examId);
    }

    /**
     * 修改考试
     * @param examUpdateForm
     * @param id
     * @return
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> updateExam(@Validated @RequestBody ExamUpdateForm examUpdateForm, @PathVariable("id") Integer id) {
        return examService.updateExam(examUpdateForm,id);
    }

    /**
     * 删除考试
     * @param ids
     * @return
     */
    @DeleteMapping("/{ids}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> deleteExam(@PathVariable("ids") @Pattern(regexp = "^\\d+(,\\d+)*$|^\\d+$") String ids) {
        return examService.deleteExam(ids);
    }

    /**
     * 教师分页查找考试列表
     * @param pageNum
     * @param pageSize
     * @param title
     * @return
     */
    @GetMapping("/paging")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<IPage<ExamVO>> getPagingExam(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                               @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                               @RequestParam(value = "title", required = false) String title) {
        return examService.getPagingExam(pageNum, pageSize, title);
    }

    /**
     * 获取考试题目id列表
     * @param id
     * @return
     */
    @GetMapping("/question/list/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<List<ExamDetailRespVO>> getQuestionList(@PathVariable("id") String id) {
        return examService.getQuestionList(id);
    }

    /**
     * 获取单题信息
     * @param examId
     * @param questionId
     * @return
     */
    @GetMapping("/question/single")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<ExamQuDetailVO> getQuestionSingle(@RequestParam("examId") Integer examId,
                                                    @RequestParam("questionId") Integer questionId) {
        return examService.getQuestionSingle(examId, questionId);
    }

    /**
     * 题目汇总
     * @param examId
     * @return
     */
    @GetMapping("/collect/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<List<ExamQuCollectVO>> getCollect(@PathVariable("id") Integer examId) {
        return examService.getCollect(examId);
    }

    /**
     * 获取考试详情信息
     * @param id
     * @return
     */
    @GetMapping("/detail")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<ExamDetailVO> getDetail(@RequestParam("id") String id) {
        return examService.getDetail(id);
    }

    /**
     * 根据班级获得考试
     * @param gradeId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<IPage<ExamVO>> getGradeExamList(@PathVariable("id") Integer gradeId,
                                                       @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                                       @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        return examService.getGradeExamList(gradeId,pageNum,pageSize);
    }

    /**
     * 考试作弊次数添加
     * @param examId
     * @return
     */
    @PutMapping("/cheat/{examId}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<String> addCheat(@PathVariable("examId") Integer examId) {
        return examService.addCheat(examId);
    }

    /**
     * 填充答案
     * @param examQuAnswerForm
     * @return
     */
    @PostMapping("/full-answer")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<ExamFillVO> addAnswer(@RequestBody ExamQuAnswerVO examQuAnswerForm) {
        return examService.addAnswer(examQuAnswerForm);
    }

    /**
     * 交卷操作
     * @param examId
     * @return
     */
    @GetMapping(value = "/hand-exam/{examId}")
    public Result<ExamQuDetailVO> handleExam(@PathVariable("examId") Integer examId) {
        return examService.handExam(examId);
    }
}