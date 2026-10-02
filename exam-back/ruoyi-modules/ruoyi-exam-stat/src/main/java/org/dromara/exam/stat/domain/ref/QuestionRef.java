package org.dromara.exam.stat.domain.ref;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * question 只读投影：题干、题型、难度、默认分值与所属题库
 *
 * <p>bankId 是一期知识点分析的入口：question → question_bank → 题库分类树。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("question")
public class QuestionRef extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long bankId;

    private String title;

    private String questionType;

    private String difficulty;

    private BigDecimal score;

    private String analysis;

    @TableLogic
    private Long delFlag;
}
