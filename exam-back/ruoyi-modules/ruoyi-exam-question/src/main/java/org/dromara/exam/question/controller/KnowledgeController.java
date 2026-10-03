package org.dromara.exam.question.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.exam.question.domain.bo.KnowledgePointBo;
import org.dromara.exam.question.domain.vo.KnowledgePointVo;
import org.dromara.exam.question.domain.vo.QuestionKnowledgeVo;
import org.dromara.exam.question.service.IKnowledgePointService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 知识点
 *
 * <p>两级：章节 → 知识点。全局共享、按租户隔离。
 * 前端访问路由地址为 /questions/knowledge，接口走网关的 /question/** 前缀。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/question/knowledge")
public class KnowledgeController extends BaseController {

    private final IKnowledgePointService knowledgePointService;

    /**
     * 查询知识点列表（平铺分页）
     */
    @SaCheckPermission("system:knowledge:list")
    @GetMapping("/list")
    public TableDataInfo<KnowledgePointVo> list(KnowledgePointBo bo, PageQuery pageQuery) {
        return knowledgePointService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询知识点树（章节 → 知识点），供管理页树表格、试题/AI 出题选择器使用
     */
    @SaCheckPermission("system:knowledge:list")
    @GetMapping("/tree")
    public R<List<KnowledgePointVo>> tree(KnowledgePointBo bo) {
        return R.ok(knowledgePointService.queryTreeList(bo));
    }

    /**
     * 按试题ID批量取知识点关联（详情页回显、错题诊断入参拼装用）
     *
     * @param ids 试题ID，逗号分隔
     */
    @SaCheckPermission("system:knowledge:list")
    @GetMapping("/byQuestions")
    public R<List<QuestionKnowledgeVo>> byQuestions(@NotBlank(message = "试题ID不能为空") @RequestParam("ids") String ids) {
        List<Long> questionIds = Arrays.stream(ids.split(","))
            .filter(StringUtils::isNotBlank)
            .map(String::trim)
            .map(Long::valueOf)
            .collect(Collectors.toList());
        return R.ok(knowledgePointService.listByQuestionIds(questionIds));
    }

    /**
     * 获取知识点详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:knowledge:query")
    @GetMapping("/{id}")
    public R<KnowledgePointVo> getInfo(@NotNull(message = "主键不能为空")
                                       @PathVariable("id") Long id) {
        return R.ok(knowledgePointService.queryById(id));
    }

    /**
     * 新增知识点
     */
    @SaCheckPermission("system:knowledge:add")
    @Log(title = "知识点", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody KnowledgePointBo bo) {
        return toAjax(knowledgePointService.insertByBo(bo));
    }

    /**
     * 修改知识点
     */
    @SaCheckPermission("system:knowledge:edit")
    @Log(title = "知识点", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody KnowledgePointBo bo) {
        return toAjax(knowledgePointService.updateByBo(bo));
    }

    /**
     * 删除知识点
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:knowledge:remove")
    @Log(title = "知识点", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(knowledgePointService.deleteWithValidByIds(List.of(ids), true));
    }
}
