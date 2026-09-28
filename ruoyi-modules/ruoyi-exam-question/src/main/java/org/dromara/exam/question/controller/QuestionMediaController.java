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
import org.dromara.exam.question.domain.vo.QuestionMediaVo;
import org.dromara.exam.question.domain.bo.QuestionMediaBo;
import org.dromara.exam.question.service.IQuestionMediaService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 试题多媒体附件
 * 前端访问路由地址为:/system/media
 *
 * @author LionLi
 * @date 2026-09-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/media")
public class QuestionMediaController extends BaseController {

    private final IQuestionMediaService questionMediaService;

    /**
     * 查询试题多媒体附件列表
     */
    @SaCheckPermission("system:media:list")
    @GetMapping("/list")
    public TableDataInfo<QuestionMediaVo> list(QuestionMediaBo bo, PageQuery pageQuery) {
        return questionMediaService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出试题多媒体附件列表
     */
    @SaCheckPermission("system:media:export")
    @Log(title = "试题多媒体附件", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(QuestionMediaBo bo, HttpServletResponse response) {
        List<QuestionMediaVo> list = questionMediaService.queryList(bo);
        ExcelUtil.exportExcel(list, "试题多媒体附件", QuestionMediaVo.class, response);
    }

    /**
     * 获取试题多媒体附件详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:media:query")
    @GetMapping("/{id}")
    public R<QuestionMediaVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(questionMediaService.queryById(id));
    }

    /**
     * 新增试题多媒体附件
     */
    @SaCheckPermission("system:media:add")
    @Log(title = "试题多媒体附件", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody QuestionMediaBo bo) {
        return toAjax(questionMediaService.insertByBo(bo));
    }

    /**
     * 修改试题多媒体附件
     */
    @SaCheckPermission("system:media:edit")
    @Log(title = "试题多媒体附件", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody QuestionMediaBo bo) {
        return toAjax(questionMediaService.updateByBo(bo));
    }

    /**
     * 删除试题多媒体附件
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:media:remove")
    @Log(title = "试题多媒体附件", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(questionMediaService.deleteWithValidByIds(List.of(ids), true));
    }
}
