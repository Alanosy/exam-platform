package org.dromara.exam.stat.domain.ref;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;

/**
 * exam_user 只读投影：白名单考试的应考人数从这里数
 *
 * @author ruoyi
 * @date 2026-10-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_user")
public class ExamUserRef extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long examId;

    private Long userId;

    @TableLogic
    private Long delFlag;
}
