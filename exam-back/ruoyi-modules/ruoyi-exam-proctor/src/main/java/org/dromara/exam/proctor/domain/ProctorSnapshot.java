package org.dromara.exam.proctor.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.util.Date;

/**
 * 摄像头抓拍对象 exam_proctor_snapshot
 *
 * <p>图片本体存在 OSS（sys_oss），这里只存 ossId 与访问地址，
 * 免得把二进制塞进业务库把表撑爆。
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_proctor_snapshot")
public class ProctorSnapshot extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 监考会话ID */
    private Long sessionId;

    /** 考试ID */
    private Long examId;

    /** 答卷记录ID */
    private Long recordId;

    /** 考生用户ID */
    private Long userId;

    /** OSS 记录ID */
    private String ossId;

    /** 抓拍图片访问地址 */
    private String url;

    /** 触发场景 periodic定时 / enter入场 / switch_screen切屏 / resume恢复 */
    private String eventType;

    /** 抓拍时间 */
    private Date captureTime;

    /** 逻辑删除 0未删 1已删 */
    private Long delFlag;
}
