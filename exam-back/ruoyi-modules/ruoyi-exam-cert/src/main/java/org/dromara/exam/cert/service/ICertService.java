package org.dromara.exam.cert.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.exam.cert.api.domain.RemoteCertIssueBo;
import org.dromara.exam.cert.api.domain.RemoteCertVo;
import org.dromara.exam.cert.domain.bo.CertIssueBo;
import org.dromara.exam.cert.domain.bo.CertificateBo;
import org.dromara.exam.cert.domain.bo.CertificateRecordBo;
import org.dromara.exam.cert.domain.vo.CertificateOptionVo;
import org.dromara.exam.cert.domain.vo.CertificateRecordVo;
import org.dromara.exam.cert.domain.vo.CertificateVo;

import java.util.List;

/**
 * 证书服务
 *
 * <p>模板归模板、颁发记录归颁发记录：模板改了不影响已发出的证书，
 * 因为颁发时把标题 / 正文 / 印章 / 背景全部快照进了记录表。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
public interface ICertService {

    /**
     * 证书模板分页列表
     */
    TableDataInfo<CertificateVo> listPage(CertificateBo bo, PageQuery pageQuery);

    /**
     * 证书模板详情
     */
    CertificateVo queryById(Long id);

    /**
     * 启用中的证书模板下拉（考试配置页选「及格证书」用）
     */
    List<CertificateOptionVo> options();

    /**
     * 新增证书模板
     *
     * @return 新模板ID
     */
    Long insert(CertificateBo bo);

    /**
     * 修改证书模板
     */
    void update(CertificateBo bo);

    /**
     * 删除证书模板
     *
     * <p>已颁发的证书不受影响（内容已快照），所以这里不做「发过就不许删」的校验。
     */
    void deleteByIds(List<Long> ids);

    /**
     * 颁发记录分页列表
     */
    TableDataInfo<CertificateRecordVo> listRecordPage(CertificateRecordBo bo, PageQuery pageQuery);

    /**
     * 我的证书（当前登录考生）
     */
    List<CertificateRecordVo> listMine();

    /**
     * 我的证书：按答卷ID查
     */
    CertificateRecordVo queryMineByRecord(Long recordId);

    /**
     * 及格自动颁发（答题服务调用，幂等）
     */
    RemoteCertVo issueOnPass(RemoteCertIssueBo bo);

    /**
     * 手工补发：成绩由证书服务去答题服务拉，不靠调用方填
     */
    RemoteCertVo issueManual(CertIssueBo bo);

    /**
     * 吊销证书
     */
    void revoke(Long id, String reason);

    /**
     * 某场考试已颁发的证书数量
     */
    Long countByExamId(Long examId);
}
