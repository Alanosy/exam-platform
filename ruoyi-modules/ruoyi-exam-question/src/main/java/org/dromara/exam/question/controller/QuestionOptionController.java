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
import org.dromara.exam.question.domain.vo.QuestionOptionVo;
import org.dromara.exam.question.domain.bo.QuestionOptionBo;
import org.dromara.exam.question.service.IQuestionOptionService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 试题选项
 * 前端访问路由地址为:/system/option
 *
 * @author LionLi
 * @date 2026-09-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/option")
public class QuestionOptionController extends BaseController {

    private final IQuestionOptionService questionOptionService;

    /**
     * 查询试题选项列表
     */
    @SaCheckPermission("system:option:list")
    @GetMapping("/list")
    public TableDataInfo<QuestionOptionVo> list(QuestionOptionBo bo, PageQuery pageQuery) {
        return questionOptionService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出试题选项列表
     */
    @SaCheckPermission("system:option:export")
    @Log(title = "试题选项", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(QuestionOptionBo bo, HttpServletResponse response) {
        List<QuestionOptionVo> list = questionOptionService.queryList(bo);
        ExcelUtil.exportExcel(list, "试题选项", QuestionOptionVo.class, response);
    }

    /**
     * 获取试题选项详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:option:query")
    @GetMapping("/{id}")
    public R<QuestionOptionVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(questionOptionService.queryById(id));
    }

    /**
     * 新增试题选项
     */
    @SaCheckPermission("system:option:add")
    @Log(title = "试题选项", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody QuestionOptionBo bo) {
        return toAjax(questionOptionService.insertByBo(bo));
    }

    /**
     * 修改试题选项
     */
    @SaCheckPermission("system:option:edit")
    @Log(title = "试题选项", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody QuestionOptionBo bo) {
        return toAjax(questionOptionService.updateByBo(bo));
    }

    /**
     * 删除试题选项
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:option:remove")
    @Log(title = "试题选项", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(questionOptionService.deleteWithValidByIds(List.of(ids), true));
    }
}
