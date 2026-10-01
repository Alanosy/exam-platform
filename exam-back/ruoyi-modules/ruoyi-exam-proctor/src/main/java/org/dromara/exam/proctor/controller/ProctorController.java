package org.dromara.exam.proctor.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.exam.proctor.domain.bo.ProctorEventQueryBo;
import org.dromara.exam.proctor.domain.bo.ProctorExamGroupBo;
import org.dromara.exam.proctor.domain.bo.ProctorSessionBo;
import org.dromara.exam.proctor.domain.vo.ProctorEventVo;
import org.dromara.exam.proctor.domain.vo.ProctorExamGroupVo;
import org.dromara.exam.proctor.domain.vo.ProctorOverviewVo;
import org.dromara.exam.proctor.domain.vo.ProctorSessionVo;
import org.dromara.exam.proctor.domain.vo.ProctorSnapshotVo;
import org.dromara.exam.proctor.service.IProctorService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 监考中心（发布者查看自己考试里的作弊记录）
 * 前端访问路由地址为:/system/proctor
 *
 * <p>类上同时注册 "" 与 "/proctor" 两个前缀：
 * 网关对 /proctor/** 是否 StripPrefix 在不同部署里配得不一样，
 * 两种都注册可以保证 /proctor/session/list 一定能命中，不必去改网关配置。
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping({"", "/proctor"})
public class ProctorController extends BaseController {

    private final IProctorService proctorService;

    /**
     * 按考试分组的监考汇总：监考中心第一层，先挑出有问题的那场考试
     */
    @SaCheckPermission("exam:proctor:list")
    @GetMapping("/exam/list")
    public R<List<ProctorExamGroupVo>> examGroupList(ProctorExamGroupBo bo) {
        return R.ok(proctorService.listExamGroups(bo));
    }

    /**
     * 监考会话列表：某场考试下的考生明细
     */
    @SaCheckPermission("exam:proctor:list")
    @GetMapping("/session/list")
    public TableDataInfo<ProctorSessionVo> sessionList(ProctorSessionBo bo, PageQuery pageQuery) {
        return proctorService.listSessionPage(bo, pageQuery);
    }

    /**
     * 某场考试的监考概览
     */
    @SaCheckPermission("exam:proctor:list")
    @GetMapping("/overview")
    public R<ProctorOverviewVo> overview(Long examId) {
        return R.ok(proctorService.overview(examId));
    }

    /**
     * 会话详情：各类计数汇总
     */
    @SaCheckPermission("exam:proctor:query")
    @GetMapping("/session/{sessionId}")
    public R<ProctorSessionVo> getSession(@PathVariable Long sessionId) {
        return R.ok(proctorService.getSession(sessionId));
    }

    /**
     * 事件流水：切屏 / 粘贴 / 退出全屏 … 每一次都在这里
     */
    @SaCheckPermission("exam:proctor:query")
    @GetMapping("/event/list")
    public TableDataInfo<ProctorEventVo> eventList(ProctorEventQueryBo bo, PageQuery pageQuery) {
        return proctorService.listEventPage(bo, pageQuery);
    }

    /**
     * 摄像头抓拍（倒序，最多 200 张）
     */
    @SaCheckPermission("exam:proctor:query")
    @GetMapping("/snapshot/list")
    public R<List<ProctorSnapshotVo>> snapshotList(Long sessionId) {
        return R.ok(proctorService.listSnapshots(sessionId));
    }
}
