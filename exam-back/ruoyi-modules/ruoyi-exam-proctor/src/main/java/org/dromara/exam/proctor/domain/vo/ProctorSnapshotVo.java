package org.dromara.exam.proctor.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.exam.proctor.domain.ProctorSnapshot;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 摄像头抓拍
 *
 * @author ruoyi
 * @date 2026-10-01
 */
@Data
@AutoMapper(target = ProctorSnapshot.class)
public class ProctorSnapshotVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 会话ID */
    private Long sessionId;

    /** 考生账号 */
    private String account;

    /** 考生姓名 */
    private String nickName;

    /** 图片地址 */
    private String url;

    /** 触发场景 periodic / enter / switch_screen / resume */
    private String eventType;

    /** 抓拍时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date captureTime;
}
