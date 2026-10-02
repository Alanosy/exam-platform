package org.dromara.exam.stat.domain.ref;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.util.Date;

/**
 * exam 表的只读投影
 *
 * <p>统计服务与 exam 同库（都是 ry-exam），读考试的主数据直接查表即可，
 * 不必绕一圈 Dubbo——统计要按考试名/时间排序分页，RPC 拉全量再排太蠢。
 * 与证书服务里读 exam.cert_id 用的是同一套路子。
 *
 * <p>只声明统计真正要用的列，不映射完整 Exam，避免把考试管理的字段耦合进来。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam")
public class ExamRef extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String examName;

    private Long paperId;

    private String examType;

    private Date startTime;

    private Date endTime;

    private String status;

    private String participantType;

    private Long creatorId;

    /** 客观题部分得分开关：统计口径要跟判分口径一致 */
    private String partialScore;

    private Integer partialScoreRate;

    /** 及格证书模板ID，仅用于展示，不参与统计 */
    private Long certId;

    @TableLogic
    private Long delFlag;
}
