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
import org.dromara.exam.question.domain.vo.QuestionBankVo;
import org.dromara.exam.question.domain.bo.QuestionBankBo;
import org.dromara.exam.question.service.IQuestionBankService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 题库
 * 前端访问路由地址为:/system/bank
 *
 * @author LionLi
 * @date 2026-09-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/question/bank")
public class QuestionBankController extends BaseController {

    private final IQuestionBankService questionBankService;

    /**
     * 查询题库列表
     */
    @SaCheckPermission("system:bank:list")
    @GetMapping("/list")
    public TableDataInfo<QuestionBankVo> list(QuestionBankBo bo, PageQuery pageQuery) {
        return questionBankService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出题库列表
     */
    @SaCheckPermission("system:bank:export")
    @Log(title = "题库", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(QuestionBankBo bo, HttpServletResponse response) {
        List<QuestionBankVo> list = questionBankService.queryList(bo);
        ExcelUtil.exportExcel(list, "题库", QuestionBankVo.class, response);
    }

    /**
     * 获取题库详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:bank:query")
    @GetMapping("/{id}")
    public R<QuestionBankVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(questionBankService.queryById(id));
    }

    /**
     * 新增题库
     */
    @SaCheckPermission("system:bank:add")
    @Log(title = "题库", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody QuestionBankBo bo) {
        return toAjax(questionBankService.insertByBo(bo));
    }

    /**
     * 修改题库
     */
    @SaCheckPermission("system:bank:edit")
    @Log(title = "题库", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody QuestionBankBo bo) {
        return toAjax(questionBankService.updateByBo(bo));
    }

    /**
     * 删除题库
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:bank:remove")
    @Log(title = "题库", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(questionBankService.deleteWithValidByIds(List.of(ids), true));
    }
}
