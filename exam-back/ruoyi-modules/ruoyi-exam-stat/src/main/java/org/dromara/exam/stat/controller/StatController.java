package org.dromara.exam.stat.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.exam.stat.domain.bo.StatExamQueryBo;
import org.dromara.exam.stat.domain.bo.StatQuestionQueryBo;
import org.dromara.exam.stat.domain.bo.StatUserQueryBo;
import org.dromara.exam.stat.domain.vo.StatAnswerVo;
import org.dromara.exam.stat.domain.vo.StatDashboardVo;
import org.dromara.exam.stat.domain.vo.StatExamRowVo;
import org.dromara.exam.stat.domain.vo.StatKnowledgeVo;
import org.dromara.exam.stat.domain.vo.StatOverviewVo;
import org.dromara.exam.stat.domain.vo.StatQuestionVo;
import org.dromara.exam.stat.domain.vo.StatSegmentVo;
import org.dromara.exam.stat.domain.vo.StatUserVo;
import org.dromara.exam.stat.service.IStatExamService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 考试统计（前端路由 /system/stat）
 *
 * <p>类上同时注册 "" 与 "/stat" 两个前缀：网关对 /stat/** 是否 StripPrefix
 * 在不同部署里配得不一样，两种都注册保证一定能命中。
 *
 * <p>这个控制器全是读，唯一的写操作是「重新计算」和「标记作废」。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping({"", "/stat"})
public class StatController extends BaseController {

    private final IStatExamService statExamService;

    /**
     * 大盘概览
     */
    @SaCheckPermission("exam:stat:list")
    @GetMapping("/dashboard")
    public R<StatDashboardVo> dashboard(StatExamQueryBo bo) {
        return R.ok(statExamService.dashboard(bo));
    }

    /**
     * 考试统计列表
     */
    @SaCheckPermission("exam:stat:list")
    @GetMapping("/exam/page")
    public TableDataInfo<StatExamRowVo> examPage(StatExamQueryBo bo) {
        return statExamService.listExamPage(bo);
    }

    /**
     * 单场考试详情
     */
    @SaCheckPermission("exam:stat:list")
    @GetMapping("/exam/{examId}/overview")
    public R<StatOverviewVo> overview(@PathVariable("examId") Long examId) {
        return R.ok(statExamService.overview(examId));
    }

    /**
     * 分数段分布
     */
    @SaCheckPermission("exam:stat:list")
    @GetMapping("/exam/{examId}/segment")
    public R<List<StatSegmentVo>> segment(@PathVariable("examId") Long examId) {
        return R.ok(statExamService.listSegment(examId));
    }

    /**
     * 考生成绩
     */
    @SaCheckPermission("exam:stat:list")
    @GetMapping("/exam/{examId}/users")
    public TableDataInfo<StatUserVo> users(@PathVariable("examId") Long examId, StatUserQueryBo bo) {
        bo.setExamId(examId);
        return statExamService.listUserPage(bo);
    }

    /**
     * 考生答卷明细
     */
    @SaCheckPermission("exam:stat:list")
    @GetMapping("/exam/{examId}/user/{userId}/detail")
    public R<StatAnswerVo> userDetail(@PathVariable("examId") Long examId,
                                      @PathVariable("userId") Long userId,
                                      @RequestParam(value = "attemptNo", required = false) Integer attemptNo) {
        return R.ok(statExamService.userDetail(examId, userId, attemptNo));
    }

    /**
     * 试题分析
     */
    @SaCheckPermission("exam:stat:list")
    @GetMapping("/exam/{examId}/questions")
    public TableDataInfo<StatQuestionVo> questions(@PathVariable("examId") Long examId, StatQuestionQueryBo bo) {
        bo.setExamId(examId);
        return statExamService.listQuestionPage(bo);
    }

    /**
     * 知识点薄弱分析
     */
    @SaCheckPermission("exam:stat:list")
    @GetMapping("/exam/{examId}/knowledge")
    public R<List<StatKnowledgeVo>> knowledge(@PathVariable("examId") Long examId) {
        return R.ok(statExamService.listKnowledge(examId));
    }

    /**
     * 手动重新计算：改完分、发现数字不对时用
     */
    @SaCheckPermission("exam:stat:recalc")
    @PostMapping("/exam/{examId}/recalc")
    public R<Void> recalc(@PathVariable("examId") Long examId) {
        statExamService.recalc(examId, "manual");
        return R.ok();
    }

    /**
     * 标记 / 取消作废：作弊、缺考等不该进统计的答卷
     */
    @SaCheckPermission("exam:stat:exclude")
    @PutMapping("/exam/{examId}/record/{recordId}/exclude")
    public R<Void> exclude(@PathVariable("examId") Long examId,
                           @PathVariable("recordId") Long recordId,
                           @RequestParam("excluded") boolean excluded) {
        statExamService.markExcluded(examId, recordId, excluded);
        return R.ok();
    }
}
