package cn.org.alan.exam.controller;


import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.ExamQuAnswer;
import cn.org.alan.exam.model.form.exam.ExamAddForm;
import cn.org.alan.exam.model.form.exam.ExamForm;
import cn.org.alan.exam.model.vo.*;
import cn.org.alan.exam.model.vo.exam.ExamQuAnswerForm;
import cn.org.alan.exam.model.vo.exam.PaperQuDetailVO;
import cn.org.alan.exam.service.IExamService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 考试管理
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@RestController
@RequestMapping("/api/exams")
public class ExamController {

    @Resource
    private IExamService examService;

    /**
     * 创建考试
     *
     * @return
     */
    @PostMapping
    public Result<String> createExam(@RequestBody ExamAddForm examAddForm) {
        return examService.createExam(examAddForm);
    }

    /**
     * 修改考试
     *
     * @return
     */
    @PutMapping("/{id}")
    public Result<String> updateExam(@RequestBody ExamForm examForm,@PathVariable("id") Integer id) {
        return examService.updateExam(examForm,id);
    }

    /**
     * 删除考试
     *
     * @return
     */
    @DeleteMapping("/{ids}")
    public Result<String> deleteExam(@PathVariable("ids") String ids) {
        return examService.deleteExam(ids);
    }

    /**
     * 教师分页查找考试列表
     *
     * @return
     */
    @GetMapping("/paging")
    public Result<IPage<ExamVO>> getPagingExam(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                               @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                               @RequestParam(value = "title", required = false) String title) {
        return examService.getPagingExam(pageNum, pageSize, title);
    }

    /**
     * 获取考试题目id列表
     *
     * @return
     */
    @GetMapping("/question/list/{id}")
    public Result<ExamDetailRespVO> getQuestionList(@PathVariable("id") String id) {
        return examService.getQuestionList(id);
    }

    /**
     * 获取单题信息
     *
     * @return
     */
    @GetMapping("/question/single")
    public Result<PaperQuDetailVO> getQuestionSingle(@RequestParam("examId") String examId,
                                                     @RequestParam("questionId") String questionId) {

        return examService.getQuestionSingle(examId, questionId);
    }

    /**
     * 题目汇总
     */
    @GetMapping("/collect/{id}")
    public Result<EXamQuCollectVO> getCollect(@PathVariable("id") String id) {
        return examService.getCollect(id);
    }

    /**
     * 获取考试详情信息
     */
    @GetMapping("/detail")
    public Result<ExamDetailVO> getDetail(@RequestParam("id") String id) {
        return examService.getDetail(id);
    }

    /**
     * 根据班级获得考试
     */
    @GetMapping("/{id}")
    public Result<ExamVO> getGradeExamList(@PathVariable("id") String id) {
        return examService.getGradeExamList(id);
    }

    /**
     * 考试作弊次数添加
     */
    @PostMapping("/cheat")
    public Result<String> addCheat() {
        return examService.addCheat();
    }

    /**
     * 填充答案
     */
    @PostMapping("/full-answer")
    public Result<ExamFillVO> addAnswer(@RequestBody ExamQuAnswerForm examQuAnswerForm) {
        return examService.addAnswer(examQuAnswerForm);
    }
}
