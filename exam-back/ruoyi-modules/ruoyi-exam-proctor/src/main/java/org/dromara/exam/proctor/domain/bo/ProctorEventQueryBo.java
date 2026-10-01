package org.dromara.exam.proctor.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 防作弊事件流水查询条件
 *
 * @author ruoyi
 */
@Data
public class ProctorEventQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 监考会话ID（与 examId 至少给一个） */
    private Long sessionId;

    /** 考试ID */
    private Long examId;

    /** 事件类型 */
    private String eventType;

    /** 级别 info / warn / danger */
    private String level;

    /** 只看需要关注的（warn + danger） */
    private Boolean onlyWarn;
}
