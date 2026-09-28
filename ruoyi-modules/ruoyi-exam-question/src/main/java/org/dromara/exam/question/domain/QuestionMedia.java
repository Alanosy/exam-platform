package org.dromara.exam.question.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 试题多媒体附件对象 question_media
 *
 * @author LionLi
 * @date 2026-09-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("question_media")
public class QuestionMedia extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 试题ID
     */
    private Long questionId;

    /**
     * media_type:image图片,audio音频,video视频
     */
    private String mediaType;

    /**
     * 资源访问地址MinIO
     */
    private String mediaUrl;

    /**
     * 原始文件名
     */
    private String mediaName;

    /**
     * 展示顺序
     */
    private Long sort;

    /**
     * 逻辑删除 0未删 1已删
     */
    @TableLogic
    private Long delFlag;


}
