package org.dromara.exam.question.domain.vo;

import org.dromara.exam.question.domain.Question;
import org.dromara.exam.question.domain.vo.QuestionOptionVo;
import cn.idev.excel.annotation.ExcelIgnore;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;


/**
 * 试题主视图对象 question
 *
 * @author LionLi
 * @date 2026-09-28
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Question.class)
public class QuestionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 所属题库ID
     */
    @ExcelProperty(value = "所属题库ID")
    private Long bankId;

    /**
     * 题干富文本
     */
    @ExcelProperty(value = "题干富文本")
    private String title;

    /**
     * 题型 SINGLE单选,MULTIPLE多选,JUDGE判断,BLANK填空,SHORT_ANSWER简答,ESSAY论述,CODE代码题,UPLOAD_FILE文件上传,MATCH匹配题
     */
    @ExcelProperty(value = "题型 SINGLE单选,MULTIPLE多选,JUDGE判断,BLANK填空,SHORT_ANSWER简答,ESSAY论述,CODE代码题,UPLOAD_FILE文件上传,MATCH匹配题")
    private String questionType;

    /**
     * 难度 easy简单 medium中等 hard困难
     */
    @ExcelProperty(value = "难度 easy简单 medium中等 hard困难")
    private String difficulty;

    /**
     * 题目默认分值
     */
    @ExcelProperty(value = "题目默认分值")
    private Long score;

    /**
     * 试题解析富文本
     */
    @ExcelProperty(value = "试题解析富文本")
    private String analysis;

    /**
     * 参考答案JSON，不同题型结构不同
     */
    @ExcelProperty(value = "参考答案JSON，不同题型结构不同")
    private String answer;

    /**
     * 题目创建人ID
     */
    @ExcelProperty(value = "题目创建人ID")
    private Long createUser;

    /**
     * 0草稿 1启用 2废弃
     */
    @ExcelProperty(value = "0草稿 1启用 2废弃")
    private Long status;

    /**
     * 选项列表，详情接口随试题一并返回（客观题使用）
     */
    @ExcelIgnore
    private List<QuestionOptionVo> options;

}
