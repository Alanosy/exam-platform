package cn.org.alan.exam.model.form;

import cn.org.alan.exam.common.result.Result;
import lombok.Data;

/**
 * @Author Alan
 * @Version
 * @Date 2024/3/28 10:44 PM
 */
@Data
public class NoticeForm {

    /**
     * ID   公告表
     */
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
