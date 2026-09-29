package org.dromara.exam.question.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 试题媒体附件保存对象（随试题一并提交）
 *
 * <p>仅供「新增 / 修改试题」接口接收前端从富文本里解析出来的媒体数据使用，
 * 不参与 question_media 单表的增删改查，因此不标注 {@code @AutoMapper}。
 *
 * @author LionLi
 * @date 2026-09-29
 */
@Data
public class QuestionMediaSaveBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 附件类型 image图片,audio音频,video视频
     */
    @NotBlank(message = "附件类型不能为空")
    private String mediaType;

    /**
     * 资源访问地址
     */
    @NotBlank(message = "资源访问地址不能为空")
    private String mediaUrl;

    /**
     * 原始文件名
     */
    private String mediaName;

    /**
     * 展示顺序
     */
    private Long sort;

}
