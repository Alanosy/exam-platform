package org.dromara.exam.cert.api;

import org.dromara.exam.cert.api.domain.RemoteCertIssueBo;
import org.dromara.exam.cert.api.domain.RemoteCertVo;

/**
 * 证书服务（跨服务调用）
 *
 * <p>证书模板与颁发记录都在 ry-exam 库，由 ruoyi-exam-cert 服务独占读写，
 * 答题服务不直连这两张表，全部走这个接口。
 *
 * <p>调用方只有两个时机：
 *   1. 交卷后（客观题卷，成绩已定）
 *   2. 阅卷完成后（含主观题，writeBackMark finished=true）
 * 两次调用都幂等：同一份答卷只会有一张证书。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
public interface RemoteCertService {

    /**
     * 及格颁发证书
     *
     * <p>内部会判断：这场考试配没配证书模板、模板是不是启用中、考生到底及格了没有。
     * 任一条件不满足就返回 null，调用方不用自己判断。
     * 已经发过的答卷直接把原证书返回，不会重复发。
     *
     * @param bo 成绩与考生信息
     * @return 证书，未配置证书 / 未及格 / 证书服务异常时返回 null
     */
    RemoteCertVo issueOnPass(RemoteCertIssueBo bo);

    /**
     * 按答卷ID查询证书
     *
     * @param recordId 答卷记录ID
     * @return 证书，没有时返回 null
     */
    RemoteCertVo queryByRecordId(Long recordId);

    /**
     * 某场考试已颁发的证书数量
     *
     * @param examId 考试ID
     * @return 数量，异常时返回 0（列表页展示用，不能因为证书服务挂了就打不开列表）
     */
    Long countByExamId(Long examId);
}
