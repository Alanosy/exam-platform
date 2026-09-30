package org.dromara.exam.paper.controller;

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
import org.dromara.exam.paper.domain.vo.PaperQuestionVo;
import org.dromara.exam.paper.domain.bo.PaperQuestionBo;
import org.dromara.exam.paper.service.IPaperQuestionService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 试卷-试题中间
 * 前端访问路由地址为:/system/question
 *
 * @author LionLi
 * @date 2026-09-29
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/paper/question")
public class PaperQuestionController extends BaseController {

    private final IPaperQuestionService paperQuestionService;

    /**
     * 查询试卷-试题中间列表
     */
    @SaCheckPermission("system:question:list")
    @GetMapping("/list")
    public TableDataInfo<PaperQuestionVo> list(PaperQuestionBo bo, PageQuery pageQuery) {
        return paperQuestionService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出试卷-试题中间列表
     */
    @SaCheckPermission("system:question:export")
    @Log(title = "试卷-试题中间", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(PaperQuestionBo bo, HttpServletResponse response) {
        List<PaperQuestionVo> list = paperQuestionService.queryList(bo);
        ExcelUtil.exportExcel(list, "试卷-试题中间", PaperQuestionVo.class, response);
    }

    /**
     * 获取试卷-试题中间详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:question:query")
    @GetMapping("/{id}")
    public R<PaperQuestionVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(paperQuestionService.queryById(id));
    }

    /**
     * 新增试卷-试题中间
     */
    @SaCheckPermission("system:question:add")
    @Log(title = "试卷-试题中间", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody PaperQuestionBo bo) {
        return toAjax(paperQuestionService.insertByBo(bo));
    }

    /**
     * 修改试卷-试题中间
     */
    @SaCheckPermission("system:question:edit")
    @Log(title = "试卷-试题中间", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody PaperQuestionBo bo) {
        return toAjax(paperQuestionService.updateByBo(bo));
    }

    /**
     * 删除试卷-试题中间
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:question:remove")
    @Log(title = "试卷-试题中间", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(paperQuestionService.deleteWithValidByIds(List.of(ids), true));
    }
}
