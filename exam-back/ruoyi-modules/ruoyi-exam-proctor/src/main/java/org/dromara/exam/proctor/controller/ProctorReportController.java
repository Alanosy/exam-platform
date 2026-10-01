package org.dromara.exam.proctor.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.dromara.exam.proctor.domain.bo.ProctorReportBo;
import org.dromara.exam.proctor.domain.bo.ProctorStartBo;
import org.dromara.exam.proctor.domain.vo.ProctorReportVo;
import org.dromara.exam.proctor.domain.vo.ProctorSessionVo;
import org.dromara.exam.proctor.domain.vo.ProctorSnapshotVo;
import org.dromara.exam.proctor.service.IProctorService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 考生端防作弊上报
 *
 * <p>只要求登录：任何参加考试的考生都要能上报，权限不能按菜单收。
 * 会话归属在服务端按 userId 校验，改 sessionId 给别人记违规是记不进去的。
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping({"", "/proctor"})
public class ProctorReportController extends BaseController {

    private final IProctorService proctorService;

    /**
     * 进入答题页：开启监考会话并拿回本场生效的防作弊规则
     */
    @SaCheckLogin
    @PostMapping("/session/start")
    public R<ProctorSessionVo> start(@Validated @RequestBody ProctorStartBo bo) {
        return R.ok(proctorService.startSession(bo));
    }

    /**
     * 批量上报事件，返回当前计数与是否已触发强制交卷
     */
    @SaCheckLogin
    @PostMapping("/event/report")
    public R<ProctorReportVo> report(@Validated @RequestBody ProctorReportBo bo) {
        return R.ok(proctorService.report(bo.getSessionId(), bo.getEvents()));
    }

    /**
     * 心跳：告诉监考端「人还在」
     */
    @SaCheckLogin
    @PostMapping("/session/heartbeat/{sessionId}")
    public R<Void> heartbeat(@PathVariable Long sessionId) {
        proctorService.heartbeat(sessionId);
        return R.ok();
    }

    /**
     * 结束会话
     *
     * @param status submitted正常交卷 / force_submit强制交卷
     */
    @SaCheckLogin
    @PostMapping("/session/finish/{recordId}")
    public R<Void> finish(@PathVariable Long recordId, @RequestParam(defaultValue = "submitted") String status) {
        proctorService.finishSession(recordId, status);
        return R.ok();
    }

    /**
     * 上传摄像头抓拍
     */
    @SaCheckLogin
    @PostMapping("/snapshot/upload")
    public R<ProctorSnapshotVo> upload(
        @RequestParam Long sessionId,
        @RequestParam(defaultValue = "periodic") String eventType,
        @RequestParam("file") MultipartFile file
    ) {
        return R.ok(proctorService.saveSnapshot(sessionId, eventType, file));
    }
}
