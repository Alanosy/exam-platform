package org.dromara.exam.proctor.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 防作弊事件上报参数（考生端批量上报）
 *
 * @author ruoyi
 */
@Data
public class ProctorEventBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 事件类型：见 ProctorEventType 的 code */
    @NotBlank(message = "事件类型不能为空")
    private String eventType;

    /** 事件摘要：粘贴内容、按下的键位等 */
    private String content;

    /** 扩展信息（JSON 串） */
    private String extra;

    /** 客户端事件发生时间 yyyy-MM-dd HH:mm:ss，为空按服务端收到时间记 */
    private String eventTime;
}
