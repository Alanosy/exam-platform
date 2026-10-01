package org.dromara.exam.cert.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.exam.cert.api.domain.RemoteCertVo;
import org.dromara.exam.cert.domain.bo.CertIssueBo;
import org.dromara.exam.cert.domain.bo.CertificateBo;
import org.dromara.exam.cert.domain.bo.CertificateRecordBo;
import org.dromara.exam.cert.domain.vo.CertificateOptionVo;
import org.dromara.exam.cert.domain.vo.CertificateRecordVo;
import org.dromara.exam.cert.domain.vo.CertificateVo;
import org.dromara.exam.cert.service.ICertService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 证书管理（发布者视角）
 *
 * <p>网关对 /cert/** 配了 StripPrefix=1，这里同时注册 "" 和 "/cert" 两个前缀，
 * 网关剥不剥前缀都能命中，跟防作弊服务保持一致。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping({"", "/cert"})
public class CertController extends BaseController {

    private final ICertService certService;

    /**
     * 证书模板列表
     */
    @SaCheckPermission("exam:cert:list")
    @GetMapping("/list")
    public TableDataInfo<CertificateVo> list(CertificateBo bo, PageQuery pageQuery) {
        return certService.listPage(bo, pageQuery);
    }

    /**
     * 启用中的证书模板下拉（考试配置页选「及格证书」用）
     */
    @SaCheckPermission("exam:cert:query")
    @GetMapping("/option/list")
    public R<List<CertificateOptionVo>> optionList() {
        return R.ok(certService.options());
    }

    /**
     * 证书模板详情
     */
    @SaCheckPermission("exam:cert:query")
    @GetMapping("/{certId}")
    public R<CertificateVo> getInfo(@PathVariable Long certId) {
        return R.ok(certService.queryById(certId));
    }

    /**
     * 新增证书模板
     */
    @SaCheckPermission("exam:cert:add")
    @Log(title = "证书管理", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Long> add(@RequestBody CertificateBo bo) {
        return R.ok(certService.insert(bo));
    }

    /**
     * 修改证书模板
     */
    @SaCheckPermission("exam:cert:edit")
    @Log(title = "证书管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@RequestBody CertificateBo bo) {
        certService.update(bo);
        return R.ok();
    }

    /**
     * 删除证书模板
     *
     * <p>已颁发的证书内容已快照，删模板不影响它们。
     */
    @SaCheckPermission("exam:cert:remove")
    @Log(title = "证书管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{certIds}")
    public R<Void> remove(@PathVariable List<Long> certIds) {
        certService.deleteByIds(certIds);
        return R.ok();
    }

    /**
     * 证书颁发记录
     */
    @SaCheckPermission("exam:cert:list")
    @GetMapping("/record/list")
    public TableDataInfo<CertificateRecordVo> recordList(CertificateRecordBo bo, PageQuery pageQuery) {
        return certService.listRecordPage(bo, pageQuery);
    }

    /**
     * 某场考试已颁发的证书数量（考试管理列表展示用）
     */
    @SaCheckPermission("exam:cert:query")
    @GetMapping("/record/count")
    public R<Long> recordCount(@RequestParam Long examId) {
        return R.ok(certService.countByExamId(examId));
    }

    /**
     * 手工补发证书
     *
     * <p>成绩由证书服务去答题服务拉，前端只给答卷ID，避免手工填分数填错。
     */
    @SaCheckPermission("exam:cert:issue")
    @Log(title = "证书管理", businessType = BusinessType.INSERT)
    @PostMapping("/record/issue")
    public R<RemoteCertVo> issue(@RequestBody CertIssueBo bo) {
        return R.ok(certService.issueManual(bo));
    }

    /**
     * 吊销证书
     */
    @SaCheckPermission("exam:cert:issue")
    @Log(title = "证书管理", businessType = BusinessType.UPDATE)
    @PutMapping("/record/revoke/{id}")
    public R<Void> revoke(@PathVariable Long id, @RequestParam(required = false) String reason) {
        certService.revoke(id, reason);
        return R.ok();
    }
}
