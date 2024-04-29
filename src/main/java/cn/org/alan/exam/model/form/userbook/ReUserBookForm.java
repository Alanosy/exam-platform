package cn.org.alan.exam.model.form.userbook;

import lombok.Data;

import java.util.List;

/**
 * @Author Alan
 * @Version
 * @Date 2024/4/25 2:39 PM
 */
@Data
public class ReUserBookForm {
    private Integer examId;
    private Integer quId;
    /**
     * 回答列表
     */
    private String answer;
}
