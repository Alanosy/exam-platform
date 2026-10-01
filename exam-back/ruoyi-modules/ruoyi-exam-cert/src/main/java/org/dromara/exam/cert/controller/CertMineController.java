package org.dromara.exam.cert.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.web.core.BaseController;
import org.dromara.exam.cert.domain.vo.CertificateRecordVo;
import org.dromara.exam.cert.service.ICertService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 我的证书（考生端）
 *
 * <p>考生只看自己的，所以全部按当前登录人过滤，不接收任何 userId 入参 ——
 * 传进来也不认，避免越权看别人的证书。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@RequiredArgsConstructor
@RestController
@RequestMapping({"", "/cert"})
public class CertMineController extends BaseController {

    private final ICertService certService;

    /**
     * 我的证书列表
     */
    @SaCheckLogin
    @GetMapping("/mine/list")
    public R<List<CertificateRecordVo>> mineList() {
        return R.ok(certService.listMine());
    }

    /**
     * 某次考试的证书（成绩页 / 考试记录详情用）
     */
    @SaCheckLogin
    @GetMapping("/mine/record/{recordId}")
    public R<CertificateRecordVo> mineByRecord(@PathVariable Long recordId) {
        return R.ok(certService.queryMineByRecord(recordId));
    }
}
