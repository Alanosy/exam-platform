package org.dromara.exam.question.domain.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dromara.exam.question.domain.vo.QuestionImportVo;

import java.io.Serial;
import java.io.Serializable;

/**
 * 导入的一行数据
 *
 * <p>带上 Excel 里的行号，校验失败时才能告诉用户「第几行错了什么」，
 * 只用下标的话中间跳过空行就会错位。
 *
 * @author LionLi
 * @date 2026-09-29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionImportRow implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Excel 中的行号，从 1 开始（含表头行，即数据行最小为 2）
     */
    private Integer rowNum;

    /**
     * 解析后的行数据
     */
    private QuestionImportVo data;

}
