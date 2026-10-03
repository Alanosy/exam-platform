package org.dromara.exam.ai.api.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 错题归因入参
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Data
public class RemoteDiagnoseBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 考生ID */
    private Long userId;

    /** 错题清单 */
    private List<RemoteAiWrongItemVo> wrongItems;

    /** 已掌握的知识点（用于对比，可空） */
    private List<String> mastered;

    /** 租户ID */
    private String tenantId;
}
