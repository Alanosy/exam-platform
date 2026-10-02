package org.dromara.exam.stat.domain.ref;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;

/**
 * question_option 只读投影：客观题选项分布用
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("question_option")
public class QuestionOptionRef extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long questionId;

    private String optionKey;

    private String optionContent;

    private Integer sort;

    @TableLogic
    private Long delFlag;
}
