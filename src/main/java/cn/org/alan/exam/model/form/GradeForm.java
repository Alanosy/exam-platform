package cn.org.alan.exam.model.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Author Alan
 * @Version
 * @Date 2024/3/28 1:49 PM
 */
@Data
public class GradeForm {
    /**
     * 班级名称
     */
    @NotBlank
    private String gradeName;

    /**
     * 班级口令
     */
    private String code;
}
