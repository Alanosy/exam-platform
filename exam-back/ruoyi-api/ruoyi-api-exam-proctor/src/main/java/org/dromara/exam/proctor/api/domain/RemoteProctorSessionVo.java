package org.dromara.exam.proctor.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 监考会话（跨服务传输用）
 *
 * @author ruoyi
 */
@Data
public class RemoteProctorSessionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 会话ID */
    private Long id;

    /** 考试ID */
    private Long examId;

    /** 考试名称 */
    private String examName;

    /** 答卷记录ID */
    private Long recordId;

    /** 考生用户ID */
    private Long userId;

    /** 考生账号 */
    private String account;

    /** 考生姓名 */
    private String nickName;

    /** 切屏次数 */
    private Integer switchCount;

    /** 粘贴次数 */
    private Integer pasteCount;

    /** 退出全屏次数 */
    private Integer exitFullscreenCount;

    /** 抓拍张数 */
    private Integer cameraCount;

    /** online作答中 / offline掉线 / submitted已交卷 / force_submit强制交卷 */
    private String status;

    /** normal正常 / suspect可疑 / serious严重 */
    private String riskLevel;

    /** 是否已触发强制交卷 0否 1是（与实体保持同类型，转换器才不用写 Long→Boolean 的映射方法） */
    private Long forceSubmit;

    /** 进入时间 */
    private Date startTime;

    /** 最近活跃时间 */
    private Date lastActiveTime;

    /** 结束时间 */
    private Date endTime;
}
