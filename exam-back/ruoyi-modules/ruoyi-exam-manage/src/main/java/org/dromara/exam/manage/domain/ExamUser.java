package org.dromara.exam.manage.domain;

import org.dromara.common.tenant.core.TenantEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serial;

/**
 * 考试白名单考生对象 exam_user
 *
 * <p>白名单考场下，被指派到这场考试的考生。一人一条，表设计上以「用户」为单位，
 * 部门只是管理后台选人时的筛选维度，不入电子信息关联表。
 *
 * @author LionLi
 * @date 2026-10-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_user")
public class ExamUser extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 考试ID
     */
    private Long examId;

    /**
     * 考生用户ID
     */
    private Long userId;

    /**
     * 逻辑删除 0未删 1已删
     */
    @TableLogic
    private Long delFlag;


}
