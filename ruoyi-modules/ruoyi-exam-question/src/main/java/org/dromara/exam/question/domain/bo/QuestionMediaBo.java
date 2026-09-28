package org.dromara.exam.question.domain.bo;

import org.dromara.exam.question.domain.QuestionMedia;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 试题多媒体附件业务对象 question_media
 *
 * @author LionLi
 * @date 2026-09-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = QuestionMedia.class, reverseConvertGenerate = false)
public class QuestionMediaBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 试题ID
     */
    @NotNull(message = "试题ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long questionId;

    /**
     * media_type:image图片,audio音频,video视频
     */
    @NotBlank(message = "media_type:image图片,audio音频,video视频不能为空", groups = { AddGroup.class, EditGroup.class })
    private String mediaType;

    /**
     * 资源访问地址MinIO
     */
    @NotBlank(message = "资源访问地址MinIO不能为空", groups = { AddGroup.class, EditGroup.class })
    private String mediaUrl;

    /**
     * 原始文件名
     */
    @NotBlank(message = "原始文件名不能为空", groups = { AddGroup.class, EditGroup.class })
    private String mediaName;

    /**
     * 展示顺序
     */
    @NotNull(message = "展示顺序不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long sort;


}
