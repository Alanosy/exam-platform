package org.dromara.exam.manage.controller;

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
import org.dromara.exam.manage.domain.vo.ExamInviteVo;
import org.dromara.exam.manage.domain.bo.ExamInviteBo;
import org.dromara.exam.manage.service.IExamInviteService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 考试邀请记录
 * 前端访问路由地址为:/system/invite
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/invite")
public class ExamInviteController extends BaseController {

    private final IExamInviteService examInviteService;

    /**
     * 查询考试邀请记录列表
     */
    @SaCheckPermission("system:invite:list")
    @GetMapping("/list")
    public TableDataInfo<ExamInviteVo> list(ExamInviteBo bo, PageQuery pageQuery) {
        return examInviteService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出考试邀请记录列表
     */
    @SaCheckPermission("system:invite:export")
    @Log(title = "考试邀请记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ExamInviteBo bo, HttpServletResponse response) {
        List<ExamInviteVo> list = examInviteService.queryList(bo);
        ExcelUtil.exportExcel(list, "考试邀请记录", ExamInviteVo.class, response);
    }

    /**
     * 获取考试邀请记录详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:invite:query")
    @GetMapping("/{id}")
    public R<ExamInviteVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(examInviteService.queryById(id));
    }

    /**
     * 新增考试邀请记录
     */
    @SaCheckPermission("system:invite:add")
    @Log(title = "考试邀请记录", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ExamInviteBo bo) {
        return toAjax(examInviteService.insertByBo(bo));
    }

    /**
     * 修改考试邀请记录
     */
    @SaCheckPermission("system:invite:edit")
    @Log(title = "考试邀请记录", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ExamInviteBo bo) {
        return toAjax(examInviteService.updateByBo(bo));
    }

    /**
     * 删除考试邀请记录
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:invite:remove")
    @Log(title = "考试邀请记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(examInviteService.deleteWithValidByIds(List.of(ids), true));
    }
}
