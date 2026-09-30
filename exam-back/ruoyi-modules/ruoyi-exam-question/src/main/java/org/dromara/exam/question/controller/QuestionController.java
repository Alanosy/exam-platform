package org.dromara.exam.question.controller;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
import org.dromara.exam.question.domain.vo.QuestionImportVo;
import org.dromara.exam.question.domain.vo.QuestionVo;
import org.dromara.exam.question.domain.bo.QuestionBo;
import org.dromara.exam.question.domain.bo.QuestionIdsBo;
import org.dromara.exam.question.domain.bo.QuestionRandomBo;
import org.dromara.exam.question.listener.QuestionImportListener;
import org.dromara.exam.question.service.IQuestionImportService;
import org.dromara.exam.question.service.IQuestionService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 试题主
 * 前端访问路由地址为:/system/question
 *
 * @author LionLi
 * @date 2026-09-28
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/question")
public class QuestionController extends BaseController {

    private final IQuestionService questionService;

    private final IQuestionImportService questionImportService;

    /**
     * 查询试题主列表
     */
    @SaCheckPermission("system:question:list")
    @GetMapping("/list")
    public TableDataInfo<QuestionVo> list(QuestionBo bo, PageQuery pageQuery) {
        return questionService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出试题主列表
     */
    @SaCheckPermission("system:question:export")
    @Log(title = "试题主", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(QuestionBo bo, HttpServletResponse response) {
        List<QuestionVo> list = questionService.queryList(bo);
        ExcelUtil.exportExcel(list, "试题主", QuestionVo.class, response);
    }

    /**
     * 下载试题导入模板
     *
     * <p>空模板 + 题库名称 / 题型 / 难度 / 状态四列下拉，用户按模板填完即可直接上传。
     */
    @SaCheckPermission("system:question:add")
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        ExcelUtil.exportExcel(new ArrayList<QuestionImportVo>(), "试题导入模板", QuestionImportVo.class, response,
            questionImportService.buildTemplateOptions());
    }

    /**
     * 批量导入试题
     *
     * <p>整批校验通过后整批入库，任意一行不通过则全部回滚，并把每一行的错误原因返回给前端。
     *
     * @param file   上传的 Excel
     * @param bankId Excel 未填写题库名称时使用的默认题库
     */
    @SaCheckPermission("system:question:add")
    @Log(title = "试题主", businessType = BusinessType.IMPORT)
    @PostMapping(value = "/importData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<String> importData(@RequestPart("file") MultipartFile file,
                                @RequestParam(value = "bankId", required = false) Long bankId) throws Exception {
        if (file.isEmpty()) {
            return R.fail("请选择要上传的文件");
        }
        QuestionImportListener listener = new QuestionImportListener();
        ExcelUtil.importExcel(file.getInputStream(), QuestionImportVo.class, listener);
        return R.ok(questionImportService.importQuestions(listener.getRows(), bankId));
    }

    /**
     * 随机抽题（组卷使用）
     *
     * <p>按题库 / 题型 / 难度筛选后随机抽取，候选不足时返回实际能抽到的全部试题。
     *
     * @param bo 抽题条件
     * @return 抽中的试题列表
     */
    @SaCheckPermission("system:question:list")
    @GetMapping("/random")
    public R<List<QuestionVo>> random(QuestionRandomBo bo) {
        return R.ok(questionService.randomQuestions(bo));
    }

    /**
     * 按ID批量查询试题（组卷回显使用），保持传入顺序
     *
     * @param bo 试题ID集合
     * @return 试题列表
     */
    @SaCheckPermission("system:question:list")
    @PostMapping("/listByIds")
    public R<List<QuestionVo>> listByIds(@Validated @RequestBody QuestionIdsBo bo) {
        return R.ok(questionService.queryByIds(bo.getIds()));
    }

    /**
     * 获取试题主详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:question:query")
    @GetMapping("/{id}")
    public R<QuestionVo> getInfo(@NotNull(message = "主键不能为空")
                                 @PathVariable("id") Long id) {
        return R.ok(questionService.queryById(id));
    }

    /**
     * 新增试题主
     */
    @SaCheckPermission("system:question:add")
    @Log(title = "试题主", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody QuestionBo bo) {
        return toAjax(questionService.insertByBo(bo));
    }

    /**
     * 新增试题（含选项）
     *
     * <p>试题与选项一次提交同时落库：先写 question 拿到主键，再批量写 question_option。
     * 答案为空时由后端按选项的 isRight 反推 rightKeys。
     *
     * @param bo 试题主（含 options）
     * @return 新建试题的主键ID
     */
    @SaCheckPermission("system:question:add")
    @Log(title = "试题主", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/create")
    public R<Long> create(@Validated(AddGroup.class) @RequestBody QuestionBo bo) {
        return R.ok("新增成功", questionService.createQuestion(bo));
    }

    /**
     * 修改试题主
     */
    @SaCheckPermission("system:question:edit")
    @Log(title = "试题主", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody QuestionBo bo) {
        return toAjax(questionService.updateByBo(bo));
    }

    /**
     * 删除试题主
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:question:remove")
    @Log(title = "试题主", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable("ids") Long[] ids) {
        return toAjax(questionService.deleteWithValidByIds(List.of(ids), true));
    }

    /**
     * 批量切换试题所属题库
     *
     * @param ids    试题主键集合
     * @param bankId 目标题库ID
     */
    @SaCheckPermission("system:question:edit")
    @Log(title = "试题主", businessType = BusinessType.UPDATE)
    @PutMapping("/changeBank")
    public R<Void> changeBank(@NotEmpty(message = "主键不能为空")
                              @RequestBody List<Long> ids,
                              @NotNull(message = "目标题库不能为空")
                              @RequestParam("bankId") Long bankId) {
        return toAjax(questionService.updateBank(ids, bankId));
    }
}
