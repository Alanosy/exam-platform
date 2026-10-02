package org.dromara.exam.stat.domain.ref;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;

/**
 * question_bank_category 只读投影：一期知识点就是题库分类树
 *
 * <p>这张表用的是 is_deleted 而不是 del_flag，所以这里不挂 @TableLogic，
 * 查询时自己带 is_deleted = 0，免得框架按 0/1 的默认值去拼条件拼错。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("question_bank_category")
public class QuestionCategoryRef extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long parentId;

    private String categoryName;

    private Integer sort;

    private Integer isDeleted;
}
