package org.dromara.exam.stat.domain.ref;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;

/**
 * question_bank 只读投影：题目 → 题库 → 分类，一期用它做知识点维度
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("question_bank")
public class QuestionBankRef extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String bankName;

    private Long categoryId;

    @TableLogic
    private Long delFlag;
}
