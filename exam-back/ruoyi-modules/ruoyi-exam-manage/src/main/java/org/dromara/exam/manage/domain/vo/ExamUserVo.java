package org.dromara.exam.manage.domain.vo;

import java.util.Date;

import org.dromara.exam.manage.domain.ExamUser;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 考试白名单考生视图对象 exam_user
 *
 * @author LionLi
 * @date 2026-10-02
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = ExamUser.class)
public class ExamUserVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 考试ID
     */
    @ExcelProperty(value = "考试ID")
    private Long examId;

    /**
     * 考生用户ID
     */
    @ExcelProperty(value = "考生用户ID")
    private Long userId;


}
