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
import org.dromara.exam.question.domain.vo.QuestionTagRelVo;
import org.dromara.exam.question.domain.bo.QuestionTagRelBo;
import org.dromara.exam.question.service.IQuestionTagRelService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 试题标签关联
 * 前端访问路由地址为:/system/tagRel
 *
 * @author LionLi
 * @date 2026-09-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/question/tagRel")
public class QuestionTagRelController extends BaseController {

    private final IQuestionTagRelService questionTagRelService;

    /**
     * 查询试题标签关联列表
     */
    @SaCheckPermission("system:tagRel:list")
    @GetMapping("/list")
    public TableDataInfo<QuestionTagRelVo> list(QuestionTagRelBo bo, PageQuery pageQuery) {
        return questionTagRelService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出试题标签关联列表
     */
    @SaCheckPermission("system:tagRel:export")
    @Log(title = "试题标签关联", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(QuestionTagRelBo bo, HttpServletResponse response) {
        List<QuestionTagRelVo> list = questionTagRelService.queryList(bo);
        ExcelUtil.exportExcel(list, "试题标签关联", QuestionTagRelVo.class, response);
    }

    /**
     * 获取试题标签关联详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:tagRel:query")
    @GetMapping("/{id}")
    public R<QuestionTagRelVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(questionTagRelService.queryById(id));
    }

    /**
     * 新增试题标签关联
     */
    @SaCheckPermission("system:tagRel:add")
    @Log(title = "试题标签关联", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody QuestionTagRelBo bo) {
        return toAjax(questionTagRelService.insertByBo(bo));
    }

    /**
     * 修改试题标签关联
     */
    @SaCheckPermission("system:tagRel:edit")
    @Log(title = "试题标签关联", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody QuestionTagRelBo bo) {
        return toAjax(questionTagRelService.updateByBo(bo));
    }

    /**
     * 删除试题标签关联
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:tagRel:remove")
    @Log(title = "试题标签关联", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(questionTagRelService.deleteWithValidByIds(List.of(ids), true));
    }
}
