package org.dromara.exam.cert.dubbo;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.exam.cert.api.RemoteCertService;
import org.dromara.exam.cert.api.domain.RemoteCertIssueBo;
import org.dromara.exam.cert.api.domain.RemoteCertVo;
import org.dromara.exam.cert.domain.CertificateRecord;
import org.dromara.exam.cert.mapper.CertificateRecordMapper;
import org.dromara.exam.cert.service.ICertService;
import org.dromara.resource.api.RemoteFileService;
import org.springframework.stereotype.Service;


/**
 * 证书服务对外实现（答题服务交卷 / 阅卷完成时调用）
 *
 * <p>发不出证书绝不能影响交卷 —— 证书服务没部署、模板被删、数据库抖动，
 * 这些情况下考生该看到成绩还是要看得到成绩，所以异常一律吞掉只记 warn，
 * 返回值给 null 让调用方继续走原来的流程。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DubboService
public class RemoteCertServiceImpl implements RemoteCertService {

    private final ICertService certService;
    private final CertificateRecordMapper recordMapper;
    private final RemoteFileService remoteFileService;

    @Override
    public RemoteCertVo issueOnPass(RemoteCertIssueBo bo) {
        try {
            return certService.issueOnPass(bo);
        } catch (Exception e) {
            log.warn("颁发证书失败 recordId={}, examId={}, {}",
                bo == null ? null : bo.getRecordId(), bo == null ? null : bo.getExamId(), e.getMessage());
            return null;
        }
    }

    @Override
    public RemoteCertVo queryByRecordId(Long recordId) {
        try {
            CertificateRecord record = recordMapper.selectOne(
                Wrappers.lambdaQuery(CertificateRecord.class)
                    .eq(CertificateRecord::getRecordId, recordId));
            if (record == null) {
                return null;
            }
            RemoteCertVo vo = MapstructUtils.convert(record, RemoteCertVo.class);
            if (vo == null) {
                return null;
            }
            // 跨服务查询也要带上图片地址，否则调用方拿到 ossId 还得再问一次文件服务
            vo.setSealUrl(urlOf(record.getSealOssId()));
            vo.setBgUrl(urlOf(record.getBgOssId()));
            return vo;
        } catch (Exception e) {
            log.warn("查询证书失败 recordId={}, {}", recordId, e.getMessage());
            return null;
        }
    }

    /**
     * ossId → 访问地址
     *
     * <p>拿不到就返回空串：图片不显示是小事，抛出去把整个成绩页打挂才是大事。
     */
    private String urlOf(String ossId) {
        if (ossId == null || ossId.isBlank()) {
            return "";
        }
        try {
            String url = remoteFileService.selectUrlByIds(ossId);
            return url == null ? "" : url;
        } catch (Exception e) {
            log.warn("查询证书图片地址失败 ossId={}, {}", ossId, e.getMessage());
            return "";
        }
    }

    @Override
    public Long countByExamId(Long examId) {
        try {
            return certService.countByExamId(examId);
        } catch (Exception e) {
            log.warn("统计证书数量失败 examId={}, {}", examId, e.getMessage());
            return 0L;
        }
    }
}
