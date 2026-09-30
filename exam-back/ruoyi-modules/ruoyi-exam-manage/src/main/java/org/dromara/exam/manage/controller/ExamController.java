package org.dromara.exam.manage.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.ObjectUtil;
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
import org.dromara.exam.manage.domain.vo.ExamVo;
import org.dromara.exam.manage.domain.vo.ExamJoinVo;
import org.dromara.exam.manage.domain.bo.ExamBo;
import org.dromara.exam.manage.domain.bo.ExamJoinBo;
import org.dromara.exam.manage.service.IExamService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 考试主
 * 前端访问路由地址为:/system/exam
 *
 * @author LionLi
 * @date 2026-09-29
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/exam")
public class ExamController extends BaseController {

    private final IExamService examService;

    /**
     * 查询考试主列表
     */
    @SaCheckPermission("system:exam:list")
    @GetMapping("/list")
    public TableDataInfo<ExamVo> list(ExamBo bo, PageQuery pageQuery) {
        return examService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出考试主列表
     */
    @SaCheckPermission("system:exam:export")
    @Log(title = "考试主", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ExamBo bo, HttpServletResponse response) {
        List<ExamVo> list = examService.queryList(bo);
        ExcelUtil.exportExcel(list, "考试主", ExamVo.class, response);
    }

    /**
     * 获取考试主详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:exam:query")
    @GetMapping("/{id}")
    public R<ExamVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(examService.queryById(id));
    }

    /**
     * 新增考试主
     */
    @SaCheckPermission("system:exam:add")
    @Log(title = "考试主", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Long> add(@Validated(AddGroup.class) @RequestBody ExamBo bo) {
        examService.insertByBo(bo);
        // 回传主键，前端保存后要留在本页展示公开考试的加入链接
        return R.ok(bo.getId());
    }

    /**
     * 修改考试主
     */
    @SaCheckPermission("system:exam:edit")
    @Log(title = "考试主", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ExamBo bo) {
        return toAjax(examService.updateByBo(bo));
    }

    /**
     * 重新生成公开考试的加入码（原加入链接立即失效）
     *
     * @param id 主键
     */
    @SaCheckPermission("system:exam:edit")
    @Log(title = "考试主", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/joinCode/refresh")
    public R<String> refreshJoinCode(@NotNull(message = "主键不能为空") @PathVariable("id") Long id) {
        return R.ok(examService.refreshJoinCode(id));
    }

    /**
     * 按加入码查询公开考试的加入信息
     *
     * <p>考生打开 /exam/join/{code} 时调用。只要求登录态，不校验菜单权限，
     * 未登录由网关 / 前端路由守卫统一拦到登录页。
     *
     * @param code 加入码
     */
    @SaCheckLogin
    @GetMapping("/join/{code}")
    public R<ExamJoinVo> joinInfo(@NotBlank(message = "加入码不能为空") @PathVariable("code") String code) {
        return R.ok(examService.queryJoinInfo(code));
    }

    /**
     * 通过加入码加入公开考试
     *
     * <p>校验参与密码、链接有效期与考试状态，通过后写一条邀请记录，
     * 供考试中心查询「我参与的考试」。
     *
     * @param code 加入码
     * @param bo   参与密码
     * @return 考试ID
     */
    @SaCheckLogin
    @RepeatSubmit()
    @PostMapping("/join/{code}")
    public R<Long> join(@NotBlank(message = "加入码不能为空") @PathVariable("code") String code, @RequestBody ExamJoinBo bo) {
        String password = ObjectUtil.isNull(bo) ? null : bo.getPassword();
        return R.ok(examService.joinByCode(code, password));
    }

    /**
     * 删除考试主
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:exam:remove")
    @Log(title = "考试主", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(examService.deleteWithValidByIds(List.of(ids), true));
    }
}
