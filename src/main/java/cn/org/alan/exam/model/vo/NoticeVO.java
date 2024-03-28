package cn.org.alan.exam.model.vo;

import lombok.Data;

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
}
