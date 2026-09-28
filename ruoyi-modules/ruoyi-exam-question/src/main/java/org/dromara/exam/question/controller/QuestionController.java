package org.dromara.exam.question.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.exam.question.domain.vo.QuestionVo;
import org.dromara.exam.question.domain.bo.QuestionBo;
import org.dromara.exam.question.service.IQuestionService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 试题主
 * 前端访问路由地址为:/system/question
 *
 * @author LionLi
 * @date 2026-09-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/question")
public class QuestionController extends BaseController {

    private final IQuestionService questionService;

    /**
     * 查询试题主列表
     */
    @SaCheckPermission("system:question:list")
    @GetMapping("/list")
    public TableDataInfo<QuestionVo> list(QuestionBo bo, PageQuery pageQuery) {
        return questionService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出试题主列表
     */
    @SaCheckPermission("system:question:export")
    @Log(title = "试题主", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(QuestionBo bo, HttpServletResponse response) {
        List<QuestionVo> list = questionService.queryList(bo);
        ExcelUtil.exportExcel(list, "试题主", QuestionVo.class, response);
    }

    /**
     * 获取试题主详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:question:query")
    @GetMapping("/{id}")
    public R<QuestionVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(questionService.queryById(id));
    }

    /**
     * 新增试题主
     */
    @SaCheckPermission("system:question:add")
    @Log(title = "试题主", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody QuestionBo bo) {
        return toAjax(questionService.insertByBo(bo));
    }

    /**
     * 修改试题主
     */
    @SaCheckPermission("system:question:edit")
    @Log(title = "试题主", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody QuestionBo bo) {
        return toAjax(questionService.updateByBo(bo));
    }

    /**
     * 删除试题主
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:question:remove")
    @Log(title = "试题主", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(questionService.deleteWithValidByIds(List.of(ids), true));
    }
}
