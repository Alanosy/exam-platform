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
import org.dromara.exam.manage.domain.vo.ExamSituationOverviewVo;
import org.dromara.exam.manage.domain.vo.ExamSituationVo;
import org.dromara.exam.manage.domain.vo.ExamWhiteUserVo;
import org.dromara.exam.manage.domain.bo.ExamBo;
import org.dromara.exam.manage.domain.bo.ExamJoinBo;
import org.dromara.exam.manage.domain.bo.ExamSituationBo;
import org.dromara.exam.manage.domain.bo.ExamUserBo;
import org.dromara.exam.manage.service.IExamService;
import org.dromara.exam.manage.service.IExamSituationService;
import org.dromara.exam.manage.service.IExamUserService;
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

    private final IExamUserService examUserService;

    private final IExamSituationService examSituationService;

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
     * 查询考试白名单考生
     *
     * <p>白名单模式下，只有这些考生能在考试中心看到本场考试。
     *
     * @param id 考试主键
     */
    @SaCheckPermission("system:exam:query")
    @GetMapping("/{id}/whiteUsers")
    public R<List<ExamWhiteUserVo>> listWhiteUsers(@NotNull(message = "主键不能为空") @PathVariable("id") Long id) {
        return R.ok(examUserService.queryWhiteUsers(id));
    }

    /**
     * 保存考试白名单
     *
     * <p>整体覆盖：传入的考生ID列表就是最终名单，不在列表里的原有考生会被移出。
     *
     * @param id 考试主键
     * @param bo 考生用户ID列表
     */
    @SaCheckPermission("system:exam:edit")
    @Log(title = "考试白名单", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/whiteUsers")
    public R<Void> saveWhiteUsers(@NotNull(message = "主键不能为空") @PathVariable("id") Long id,
                                  @RequestBody ExamUserBo bo) {
        if (ObjectUtil.isNull(bo)) {
            bo = new ExamUserBo();
        }
        bo.setExamId(id);
        examUserService.saveWhiteUsers(bo);
        return R.ok();
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

    /* ---------------------------------- 考试情况 ---------------------------------- */

    /**
     * 整场考试的概览：应考 / 参考 / 已交卷 / 待阅 / 平均分 / 及格率
     *
     * <p>实时从答卷记录算，不等统计任务的预计算结果——管理端点开就得看得见数。
     */
    @SaCheckPermission("system:exam:query")
    @GetMapping("/{id}/situation/overview")
    public R<ExamSituationOverviewVo> situationOverview(@NotNull(message = "主键不能为空") @PathVariable("id") Long id) {
        return R.ok(examSituationService.overview(id));
    }

    /**
     * 参考名单与成绩
     *
     * <p>谁参考了、考了多少分、主观题阅完没有，都在这一页。
     */
    @SaCheckPermission("system:exam:query")
    @GetMapping("/{id}/situation/records")
    public TableDataInfo<ExamSituationVo> situationRecords(@NotNull(message = "主键不能为空") @PathVariable("id") Long id,
                                                           ExamSituationBo bo, PageQuery pageQuery) {
        ExamSituationBo target = ObjectUtil.defaultIfNull(bo, new ExamSituationBo());
        target.setExamId(id);
        return examSituationService.listSituation(target, pageQuery);
    }

    /**
     * 导出整场考试情况：一张「成绩概览」+ 一张「考生明细」
     *
     * <p>筛选条件与页面上保持一致，页面上筛出来的就是导出来的。
     */
    @SaCheckPermission("system:exam:export")
    @Log(title = "考试情况", businessType = BusinessType.EXPORT)
    @PostMapping("/{id}/situation/export")
    public void exportSituation(@NotNull(message = "主键不能为空") @PathVariable("id") Long id,
                                ExamSituationBo bo, HttpServletResponse response) {
        ExamSituationBo target = ObjectUtil.defaultIfNull(bo, new ExamSituationBo());
        target.setExamId(id);
        examSituationService.export(target, response);
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
