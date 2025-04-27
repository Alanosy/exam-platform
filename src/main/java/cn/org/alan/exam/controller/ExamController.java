package cn.org.alan.exam.controller;


import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.exam.ExamAddForm;
import cn.org.alan.exam.model.form.exam.ExamUpdateForm;
import cn.org.alan.exam.model.form.exam_qu_answer.ExamQuAnswerAddForm;
import cn.org.alan.exam.model.vo.exam.*;
import cn.org.alan.exam.model.vo.record.ExamRecordDetailVO;
import cn.org.alan.exam.service.IExamService;
import com.baomidou.mybatisplus.core.metadata.IPage;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(tags = "考试管理相关接口")
@RestController
@RequestMapping("/api/exams")
public class ExamController {

    @Resource
    private IExamService examService;

    /**
     * 创建考试
     *
     * @param examAddForm
     * @return
     */
    @ApiOperation("创建考试")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> createExam(@Validated @RequestBody ExamAddForm examAddForm) {
        return examService.createExam(examAddForm);
    }

    /**
     * 开始考试
     *
     * @param examId 试卷ID
     * @return
     */
    @ApiOperation("开始考试")
    @GetMapping("/start")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<String> startExam(@RequestParam("examId") @NotNull Integer examId) {
        return examService.startExam(examId);
    }

    /**
     * 修改考试
     *
     * @param examUpdateForm
     * @param id             试卷ID
     * @return
     */
    @ApiOperation("修改考试")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> updateExam(@Validated @RequestBody ExamUpdateForm examUpdateForm, @PathVariable("id") @NotNull Integer id) {
        return examService.updateExam(examUpdateForm, id);
    }

    /**
     * 删除考试
     *
     * @param ids 试卷ID
     * @return
     */
    @ApiOperation("删除考试")
    @DeleteMapping("/{ids}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<String> deleteExam(@PathVariable("ids") @Pattern(regexp = "^\\d+(,\\d+)*$|^\\d+$") String ids) {
        return examService.deleteExam(ids);
    }

    /**
     * 教师分页查找考试列表
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param title    试卷标题
     * @return
     */
    @ApiOperation("教师分页查找考试列表")
    @GetMapping("/paging")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<IPage<ExamVO>> getPagingExam(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                               @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                               @RequestParam(value = "title", required = false) String title) {
        return examService.getPagingExam(pageNum, pageSize, title);
    }

    /**
     * 获取考试题目id列表
     *
     * @param examId 试卷ID
     * @return
     */
    @ApiOperation("获取考试题目id列表")
    @GetMapping("/question/list/{examId}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<ExamQuestionListVO> getQuestionList(@PathVariable("examId") @NotBlank Integer examId) {
        return examService.getQuestionList(examId);
    }

    /**
     * 获取单题信息
     *
     * @param examId     试卷ID
     * @param questionId 试题ID
     * @return
     */
    @ApiOperation("获取单题信息")
    @GetMapping("/question/single")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<ExamQuDetailVO> getQuestionSingle(@RequestParam("examId") Integer examId,
                                                    @RequestParam("questionId") Integer questionId) {
        return examService.getQuestionSingle(examId, questionId);
    }

    /**
     * 题目汇总
     *
     * @param examId 试卷ID
     * @return
     */
    @ApiOperation("题目汇总")
    @GetMapping("/collect/{id}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<List<ExamQuCollectVO>> getCollect(@PathVariable("id") @NotNull Integer examId) {
        return examService.getCollect(examId);
    }


    /**
     * 获取考试详情信息
     *
     * @param examId 试卷ID
     * @return
     */
    @ApiOperation("获取考试详情信息")
    @GetMapping("/detail")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<ExamDetailVO> getDetail(@RequestParam("examId") @NotBlank Integer examId) {
        return examService.getDetail(examId);
    }

    /**
     * 根据班级获得考试
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param title    试卷标题
     * @param isASC    是否升序排列，true为升序，false为降序，默认为false
     * @return
     */
    @ApiOperation("根据班级获得考试")
    @GetMapping("/grade")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<IPage<ExamGradeListVO>> getGradeExamList(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                                           @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                                           @RequestParam(value = "title", required = false) String title,
                                                           @RequestParam(value = "isASC", required = false, defaultValue = "false") Boolean isASC) {
        return examService.getGradeExamList(pageNum, pageSize, title, isASC);
    }

    /**
     * 考试作弊次数添加
     *
     * @param examId 试卷ID
     * @return
     */
    @ApiOperation("考试作弊次数添加")
    @PutMapping("/cheat/{examId}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<Integer> addCheat(@PathVariable("examId") @NotNull Integer examId) {
        return examService.addCheat(examId);
    }

    /**
     * 填充答案
     *
     * @param examQuAnswerForm
     * @return
     */
    @ApiOperation("填充答案")
    @PostMapping("/full-answer")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<String> addAnswer(@Validated @RequestBody ExamQuAnswerAddForm examQuAnswerForm) {
        return examService.addAnswer(examQuAnswerForm);
    }

    /**
     * 交卷操作
     *
     * @param examId 试卷ID
     * @return
     */
    @ApiOperation("交卷操作")
    @GetMapping(value = "/hand-exam/{examId}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<ExamQuDetailVO> handleExam(@PathVariable("examId") @NotNull Integer examId) {
        return examService.handExam(examId);
    }

    /**
     * 查看详情
     *
     * @param examId 试卷ID
     * @return
     */
    @ApiOperation("交卷操作")
    @GetMapping(value = "/details/{examId}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin','role_student')")
    public Result<List<ExamRecordDetailVO>> details(@PathVariable("examId") @NotNull Integer examId) {
        return examService.details(examId);
    }
}