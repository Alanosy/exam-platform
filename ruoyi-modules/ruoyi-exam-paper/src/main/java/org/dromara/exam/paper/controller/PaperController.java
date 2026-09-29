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
import org.dromara.exam.paper.domain.vo.PaperVo;
import org.dromara.exam.paper.domain.bo.PaperBo;
import org.dromara.exam.paper.service.IPaperService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 试卷主
 * 前端访问路由地址为:/system/paper
 *
 * @author LionLi
 * @date 2026-09-29
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/paper")
public class PaperController extends BaseController {

    private final IPaperService paperService;

    /**
     * 查询试卷主列表
     */
    @SaCheckPermission("system:paper:list")
    @GetMapping("/list")
    public TableDataInfo<PaperVo> list(PaperBo bo, PageQuery pageQuery) {
        return paperService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出试卷主列表
     */
    @SaCheckPermission("system:paper:export")
    @Log(title = "试卷主", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(PaperBo bo, HttpServletResponse response) {
        List<PaperVo> list = paperService.queryList(bo);
        ExcelUtil.exportExcel(list, "试卷主", PaperVo.class, response);
    }

    /**
     * 获取试卷主详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:paper:query")
    @GetMapping("/{id}")
    public R<PaperVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(paperService.queryById(id));
    }

    /**
     * 新增试卷主
     */
    @SaCheckPermission("system:paper:add")
    @Log(title = "试卷主", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody PaperBo bo) {
        return toAjax(paperService.insertByBo(bo));
    }

    /**
     * 组卷保存：试卷信息 + 已选试题一次提交
     *
     * <p>有主键走修改、无主键走新增，试题明细按传入顺序全量覆盖写入 paper_question。
     *
     * @param bo 试卷信息（含 questions 试题明细）
     * @return 试卷主键ID
     */
    @SaCheckPermission("system:paper:edit")
    @Log(title = "试卷主", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/save")
    public R<Long> save(@RequestBody PaperBo bo) {
        return R.ok("组卷保存成功", paperService.saveWithQuestions(bo));
    }

    /**
     * 修改试卷主
     */
    @SaCheckPermission("system:paper:edit")
    @Log(title = "试卷主", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody PaperBo bo) {
        return toAjax(paperService.updateByBo(bo));
    }

    /**
     * 删除试卷主
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:paper:remove")
    @Log(title = "试卷主", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(paperService.deleteWithValidByIds(List.of(ids), true));
    }
}
