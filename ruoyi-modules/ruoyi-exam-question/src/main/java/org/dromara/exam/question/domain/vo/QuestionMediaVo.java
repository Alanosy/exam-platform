package org.dromara.exam.question.domain.vo;

import org.dromara.exam.question.domain.QuestionMedia;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 试题多媒体附件视图对象 question_media
 *
 * @author LionLi
 * @date 2026-09-28
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = QuestionMedia.class)
public class QuestionMediaVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 试题ID
     */
    @ExcelProperty(value = "试题ID")
    private Long questionId;

    /**
     * media_type:image图片,audio音频,video视频
     */
    @ExcelProperty(value = "media_type:image图片,audio音频,video视频")
    private String mediaType;

    /**
     * 资源访问地址MinIO
     */
    @ExcelProperty(value = "资源访问地址MinIO")
    private String mediaUrl;

    /**
     * 原始文件名
     */
    @ExcelProperty(value = "原始文件名")
    private String mediaName;

    /**
     * 展示顺序
     */
    @ExcelProperty(value = "展示顺序")
    private Long sort;


}
