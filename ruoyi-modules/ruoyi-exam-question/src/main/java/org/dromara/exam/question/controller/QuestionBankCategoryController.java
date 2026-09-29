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
import org.dromara.exam.question.domain.vo.QuestionBankCategoryVo;
import org.dromara.exam.question.domain.bo.QuestionBankCategoryBo;
import org.dromara.exam.question.service.IQuestionBankCategoryService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 题库分类目录
 * 前端访问路由地址为:/system/bankCategory
 * 接口地址统一走网关的 /question/** 前缀，与其他题库接口保持一致
 *
 * @author LionLi
 * @date 2026-09-29
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/question/bankCategory")
public class QuestionBankCategoryController extends BaseController {

    private final IQuestionBankCategoryService questionBankCategoryService;

    /**
     * 查询题库分类目录列表
     */
    @SaCheckPermission("system:bankCategory:list")
    @GetMapping("/list")
    public TableDataInfo<QuestionBankCategoryVo> list(QuestionBankCategoryBo bo, PageQuery pageQuery) {
        return questionBankCategoryService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询题库分类目录树（全量，供树表格与树选择使用）
     */
    @SaCheckPermission("system:bankCategory:list")
    @GetMapping("/tree")
    public R<List<QuestionBankCategoryVo>> tree(QuestionBankCategoryBo bo) {
        return R.ok(questionBankCategoryService.queryTreeList(bo));
    }

    /**
     * 导出题库分类目录列表
     */
    @SaCheckPermission("system:bankCategory:export")
    @Log(title = "题库分类目录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(QuestionBankCategoryBo bo, HttpServletResponse response) {
        List<QuestionBankCategoryVo> list = questionBankCategoryService.queryList(bo);
        ExcelUtil.exportExcel(list, "题库分类目录", QuestionBankCategoryVo.class, response);
    }

    /**
     * 获取题库分类目录详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:bankCategory:query")
    @GetMapping("/{id}")
    public R<QuestionBankCategoryVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable("id") Long id) {
        return R.ok(questionBankCategoryService.queryById(id));
    }

    /**
     * 新增题库分类目录
     */
    @SaCheckPermission("system:bankCategory:add")
    @Log(title = "题库分类目录", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody QuestionBankCategoryBo bo) {
        return toAjax(questionBankCategoryService.insertByBo(bo));
    }

    /**
     * 修改题库分类目录
     */
    @SaCheckPermission("system:bankCategory:edit")
    @Log(title = "题库分类目录", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody QuestionBankCategoryBo bo) {
        return toAjax(questionBankCategoryService.updateByBo(bo));
    }

    /**
     * 删除题库分类目录
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:bankCategory:remove")
    @Log(title = "题库分类目录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(questionBankCategoryService.deleteWithValidByIds(List.of(ids), true));
    }
}
