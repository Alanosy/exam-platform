package org.dromara.exam.answer.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.exam.answer.domain.bo.AnswerSaveBo;
import org.dromara.exam.answer.domain.bo.ExamRecordBo;
import org.dromara.exam.answer.domain.vo.ExamCenterVo;
import org.dromara.exam.answer.domain.vo.ExamPaperVo;
import org.dromara.exam.answer.domain.vo.ExamRecordVo;
import org.dromara.exam.answer.domain.vo.ExamResultVo;
import org.dromara.exam.answer.service.IExamRecordService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 考试答卷
 * 前端访问路由地址为:/exam/center
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/record")
public class ExamRecordController extends BaseController {

    private final IExamRecordService examRecordService;

    /**
     * 我的考试列表（考试中心）
     */
    @SaCheckLogin
    @GetMapping("/center")
    public R<List<ExamCenterVo>> center() {
        return R.ok(examRecordService.listMyCenter());
    }

    /**
     * 我的考试记录（分页）
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     */
    @SaCheckLogin
    @GetMapping("/records")
    public TableDataInfo<ExamRecordVo> records(ExamRecordBo bo, PageQuery pageQuery) {
        return examRecordService.listMyRecords(bo, pageQuery);
    }

    /**
     * 开始 / 继续考试
     *
     * @param examId 考试ID
     * @return 答卷记录ID
     */
    @SaCheckLogin
    @Log(title = "考试答卷", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/start/{examId}")
    public R<Long> start(@NotNull(message = "考试ID不能为空") @PathVariable("examId") Long examId) {
        return R.ok(examRecordService.startExam(examId));
    }

    /**
     * 取答题页数据
     *
     * @param recordId 答卷记录ID
     */
    @SaCheckLogin
    @GetMapping("/{recordId}/paper")
    public R<ExamPaperVo> paper(@NotNull(message = "答卷ID不能为空") @PathVariable("recordId") Long recordId) {
        return R.ok(examRecordService.getPaper(recordId));
    }

    /**
     * 保存单题作答
     *
     * @param recordId 答卷记录ID
     * @param bo       作答内容
     */
    @SaCheckLogin
    @PostMapping("/{recordId}/answer")
    public R<Void> saveAnswer(@NotNull(message = "答卷ID不能为空") @PathVariable("recordId") Long recordId,
                              @Validated(AddGroup.class) @RequestBody AnswerSaveBo bo) {
        examRecordService.saveAnswer(recordId, bo);
        return R.ok();
    }

    /**
     * 交卷
     *
     * @param recordId 答卷记录ID
     */
    @SaCheckLogin
    @Log(title = "考试答卷", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{recordId}/submit")
    public R<ExamResultVo> submit(@NotNull(message = "答卷ID不能为空") @PathVariable("recordId") Long recordId) {
        return R.ok(examRecordService.submit(recordId));
    }

    /**
     * 查询成绩
     *
     * @param recordId 答卷记录ID
     */
    @SaCheckLogin
    @GetMapping("/{recordId}/result")
    public R<ExamResultVo> result(@NotNull(message = "答卷ID不能为空") @PathVariable("recordId") Long recordId) {
        return R.ok(examRecordService.getResult(recordId));
    }
}
