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
import org.dromara.exam.question.domain.vo.QuestionTagVo;
import org.dromara.exam.question.domain.bo.QuestionTagBo;
import org.dromara.exam.question.service.IQuestionTagService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 试题标签
 * 前端访问路由地址为:/system/tag
 *
 * @author LionLi
 * @date 2026-09-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/tag")
public class QuestionTagController extends BaseController {

    private final IQuestionTagService questionTagService;

    /**
     * 查询试题标签列表
     */
    @SaCheckPermission("system:tag:list")
    @GetMapping("/list")
    public TableDataInfo<QuestionTagVo> list(QuestionTagBo bo, PageQuery pageQuery) {
        return questionTagService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出试题标签列表
     */
    @SaCheckPermission("system:tag:export")
    @Log(title = "试题标签", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(QuestionTagBo bo, HttpServletResponse response) {
        List<QuestionTagVo> list = questionTagService.queryList(bo);
        ExcelUtil.exportExcel(list, "试题标签", QuestionTagVo.class, response);
    }

    /**
     * 获取试题标签详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:tag:query")
    @GetMapping("/{id}")
    public R<QuestionTagVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(questionTagService.queryById(id));
    }

    /**
     * 新增试题标签
     */
    @SaCheckPermission("system:tag:add")
    @Log(title = "试题标签", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody QuestionTagBo bo) {
        return toAjax(questionTagService.insertByBo(bo));
    }

    /**
     * 修改试题标签
     */
    @SaCheckPermission("system:tag:edit")
    @Log(title = "试题标签", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody QuestionTagBo bo) {
        return toAjax(questionTagService.updateByBo(bo));
    }

    /**
     * 删除试题标签
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:tag:remove")
    @Log(title = "试题标签", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(questionTagService.deleteWithValidByIds(List.of(ids), true));
    }
}
