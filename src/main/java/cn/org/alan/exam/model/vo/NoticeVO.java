package cn.org.alan.exam.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author Alan
 * @Version
 * @Date 2024/3/28 11:26 PM
 */
@Data
public class NoticeVO {
    private Integer id;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 图片地址
     */
    private String image;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 创建用户ID   唯一
     */
    private Integer userId;

    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
