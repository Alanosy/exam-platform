package org.dromara.exam.mark.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.exam.mark.domain.bo.MarkExamBo;
import org.dromara.exam.mark.domain.bo.MarkScoreBo;
import org.dromara.exam.mark.domain.bo.MarkTaskBo;
import org.dromara.exam.mark.domain.vo.MarkExamVo;
import org.dromara.exam.mark.domain.vo.MarkLogVo;
import org.dromara.exam.mark.domain.vo.MarkQuestionVo;
import org.dromara.exam.mark.domain.vo.MarkTaskVo;
import org.dromara.exam.mark.service.IMarkService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 阅卷管理
 * 前端访问路由地址为:/system/mark
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/mark")
public class MarkController extends BaseController {

    private final IMarkService markService;

    /**
     * 阅卷列表：按考试聚合，只统计正式考试
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     */
    @SaCheckPermission("exam:mark:list")
    @GetMapping("/exam/list")
    public TableDataInfo<MarkExamVo> examList(MarkExamBo bo, PageQuery pageQuery) {
        return markService.listExamPage(bo, pageQuery);
    }

    /**
     * 某场考试下的答卷列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     */
    @SaCheckPermission("exam:mark:list")
    @GetMapping("/task/list")
    public TableDataInfo<MarkTaskVo> taskList(MarkTaskBo bo, PageQuery pageQuery) {
        return markService.listTaskPage(bo, pageQuery);
    }

    /**
     * 阅卷页：主观题明细
     *
     * @param taskId 阅卷任务ID
     */
    @SaCheckPermission("exam:mark:list")
    @GetMapping("/task/{taskId}/questions")
    public R<List<MarkQuestionVo>> questions(@NotNull(message = "阅卷任务ID不能为空") @PathVariable("taskId") Long taskId) {
        return R.ok(markService.listQuestions(taskId));
    }

    /**
     * 阅卷操作日志
     *
     * @param taskId 阅卷任务ID
     */
    @SaCheckPermission("exam:mark:list")
    @GetMapping("/task/{taskId}/logs")
    public R<List<MarkLogVo>> logs(@NotNull(message = "阅卷任务ID不能为空") @PathVariable("taskId") Long taskId) {
        return R.ok(markService.listLogs(taskId));
    }

    /**
     * 单题打分
     *
     * @param bo 打分入参
     */
    @SaCheckPermission("exam:mark:edit")
    @Log(title = "阅卷", businessType = BusinessType.UPDATE)
    @PostMapping("/score")
    public R<Void> score(@Validated(AddGroup.class) @RequestBody MarkScoreBo bo) {
        markService.score(bo);
        return R.ok();
    }

    /**
     * 确认完成阅卷
     *
     * @param taskId 阅卷任务ID
     */
    @SaCheckPermission("exam:mark:edit")
    @Log(title = "阅卷", businessType = BusinessType.UPDATE)
    @PostMapping("/task/{taskId}/finish")
    public R<Void> finish(@NotNull(message = "阅卷任务ID不能为空") @PathVariable("taskId") Long taskId) {
        markService.finish(taskId);
        return R.ok();
    }

    /**
     * AI 批量预评：只给建议分与理由，教师确认后才作为最终得分
     *
     * @param taskId 阅卷任务ID
     */
    @SaCheckLogin
    @Log(title = "阅卷", businessType = BusinessType.UPDATE)
    @PostMapping("/task/{taskId}/ai")
    public R<Void> aiPreview(@NotNull(message = "阅卷任务ID不能为空") @PathVariable("taskId") Long taskId) {
        markService.aiPreview(taskId);
        return R.ok();
    }
}
