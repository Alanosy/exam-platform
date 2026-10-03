package org.dromara.exam.question.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 批量保存试题入参（跨服务传输用）
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class RemoteQuestionSaveBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 目标题库ID */
    private Long bankId;

    /** 入库状态 0草稿 1启用 2废弃；AI 生成的题默认落草稿，避免直接进卷 */
    private String status;

    /** 创建人用户ID，为空时取调用方上下文 */
    private Long createUser;

    /** 租户ID，Dubbo 调用没有登录上下文，必须由调用方显式传入 */
    private String tenantId;

    /** 待保存的试题 */
    private List<RemoteQuestionSaveItem> questions;
}
