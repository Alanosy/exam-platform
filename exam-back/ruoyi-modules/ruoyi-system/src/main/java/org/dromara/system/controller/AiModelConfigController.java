package org.dromara.system.controller;

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
import org.dromara.system.domain.vo.AiModelConfigVo;
import org.dromara.system.domain.bo.AiModelConfigBo;
import org.dromara.system.service.IAiModelConfigService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * AI大模型配置
 * 前端访问路由地址为:/system/modelConfig
 *
 * @author Alan
 * @date 2026-10-01
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/modelConfig")
public class AiModelConfigController extends BaseController {

    private final IAiModelConfigService aiModelConfigService;

    /**
     * 查询AI大模型配置列表
     */
    @SaCheckPermission("system:modelConfig:list")
    @GetMapping("/list")
    public TableDataInfo<AiModelConfigVo> list(AiModelConfigBo bo, PageQuery pageQuery) {
        return aiModelConfigService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出AI大模型配置列表
     */
    @SaCheckPermission("system:modelConfig:export")
    @Log(title = "AI大模型配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(AiModelConfigBo bo, HttpServletResponse response) {
        List<AiModelConfigVo> list = aiModelConfigService.queryList(bo);
        ExcelUtil.exportExcel(list, "AI大模型配置", AiModelConfigVo.class, response);
    }

    /**
     * 获取AI大模型配置详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:modelConfig:query")
    @GetMapping("/{id}")
    public R<AiModelConfigVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(aiModelConfigService.queryById(id));
    }

    /**
     * 新增AI大模型配置
     */
    @SaCheckPermission("system:modelConfig:add")
    @Log(title = "AI大模型配置", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody AiModelConfigBo bo) {
        return toAjax(aiModelConfigService.insertByBo(bo));
    }

    /**
     * 修改AI大模型配置
     */
    @SaCheckPermission("system:modelConfig:edit")
    @Log(title = "AI大模型配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody AiModelConfigBo bo) {
        return toAjax(aiModelConfigService.updateByBo(bo));
    }

    /**
     * 删除AI大模型配置
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:modelConfig:remove")
    @Log(title = "AI大模型配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(aiModelConfigService.deleteWithValidByIds(List.of(ids), true));
    }
}
