package org.dromara.exam.proctor.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.exam.proctor.domain.ProctorEvent;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 防作弊事件流水
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Data
@AutoMapper(target = ProctorEvent.class)
public class ProctorEventVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 会话ID */
    private Long sessionId;

    /** 考试ID */
    private Long examId;

    /** 答卷记录ID */
    private Long recordId;

    /** 考生账号 */
    private String account;

    /** 考生姓名 */
    private String nickName;

    /** 事件类型 */
    private String eventType;

    /** 事件名称 */
    private String eventName;

    /** 级别 info / warn / danger */
    private String level;

    /** 事件摘要 */
    private String content;

    /** 扩展信息 */
    private String extra;

    /** 客户端事件时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date eventTime;

    /** 入库时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
