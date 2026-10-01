package org.dromara.exam.proctor.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.util.Date;

/**
 * 防作弊事件对象 exam_proctor_event
 *
 * <p>每一次可疑动作一条：切屏、粘贴、退出全屏、开开发者工具……
 * 会话上的计数由这些流水累加而来，两边可能对不上（比如重复上报被去重），以流水为准排查。
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_proctor_event")
public class ProctorEvent extends TenantEntity {

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

    /** 事件类型 switch_screen / paste / exit_fullscreen ... */
    private String eventType;

    /** 事件名称（中文，列表直接展示） */
    private String eventName;

    /** 级别 info提示 / warn可疑 / danger严重 */
    private String level;

    /** 事件摘要，如被粘贴内容的前 200 字 */
    private String content;

    /** 扩展信息（键位、屏幕尺寸等） */
    private String extra;

    /** 事件发生的客户端时间 */
    private Date eventTime;

    /** 逻辑删除 0未删 1已删 */
    private Long delFlag;
}
