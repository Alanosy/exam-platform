package org.dromara.exam.practice.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.exam.practice.domain.bo.WrongNoteBo;
import org.dromara.exam.practice.domain.bo.WrongQuestionBo;
import org.dromara.exam.practice.domain.bo.WrongReviewBo;
import org.dromara.exam.practice.domain.bo.WrongSourceBo;
import org.dromara.exam.practice.domain.vo.WrongOverviewVo;
import org.dromara.exam.practice.domain.vo.WrongQuestionVo;
import org.dromara.exam.practice.domain.vo.WrongReviewRecordVo;
import org.dromara.exam.practice.domain.vo.WrongReviewResultVo;
import org.dromara.exam.practice.domain.vo.WrongSourceVo;
import org.dromara.exam.practice.service.IWrongQuestionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 错题本
 * 前端访问路由地址为:/exam/wrong
 *
 * @author ruoyi
 * @date 2026-09-30
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/wrong")
public class WrongQuestionController extends BaseController {

    private final IWrongQuestionService wrongQuestionService;

    /**
     * 错题本总览统计
     */
    @SaCheckLogin
    @GetMapping("/overview")
    public R<WrongOverviewVo> overview() {
        return R.ok(wrongQuestionService.overview());
    }

    /**
     * 按来源分组：每场考试 / 每份练习各错了多少题
     */
    @SaCheckLogin
    @GetMapping("/sources")
    public R<List<WrongSourceVo>> sources() {
        return R.ok(wrongQuestionService.listSources());
    }

    /**
     * 按来源分页：错题本首页只列到「哪场考试错了多少题」，点进去才看明细
     *
     * @param bo        来源维度筛选条件
     * @param pageQuery 分页参数
     */
    @SaCheckLogin
    @GetMapping("/sources/page")
    public TableDataInfo<WrongSourceVo> sourcesPage(WrongSourceBo bo, PageQuery pageQuery) {
        return wrongQuestionService.listSourcesPage(bo, pageQuery);
    }

    /**
     * 我的错题（分页）
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     */
    @SaCheckLogin
    @GetMapping("/list")
    public TableDataInfo<WrongQuestionVo> list(WrongQuestionBo bo, PageQuery pageQuery) {
        return wrongQuestionService.listMyWrong(bo, pageQuery);
    }

    /**
     * 错题详情
     *
     * @param id 错题记录ID
     */
    @SaCheckLogin
    @GetMapping("/{id}")
    public R<WrongQuestionVo> detail(@NotNull(message = "错题ID不能为空") @PathVariable("id") Long id) {
        return R.ok(wrongQuestionService.queryById(id));
    }

    /**
     * 某道错题的重做历史
     *
     * @param id 错题记录ID
     */
    @SaCheckLogin
    @GetMapping("/{id}/reviews")
    public R<List<WrongReviewRecordVo>> reviews(@NotNull(message = "错题ID不能为空") @PathVariable("id") Long id) {
        return R.ok(wrongQuestionService.listReviews(id));
    }

    /**
     * 重做错题：判分并返回正确答案与解析
     *
     * @param id 错题记录ID
     * @param bo 本次作答
     */
    @SaCheckLogin
    @Log(title = "错题本", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/{id}/review")
    public R<WrongReviewResultVo> review(@NotNull(message = "错题ID不能为空") @PathVariable("id") Long id,
                                         @Validated(AddGroup.class) @RequestBody WrongReviewBo bo) {
        return R.ok(wrongQuestionService.review(id, bo));
    }

    /**
     * 保存错题笔记
     *
     * @param id 错题记录ID
     * @param bo 笔记内容
     */
    @SaCheckLogin
    @Log(title = "错题本", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/note")
    public R<Void> note(@NotNull(message = "错题ID不能为空") @PathVariable("id") Long id,
                        @Validated @RequestBody WrongNoteBo bo) {
        wrongQuestionService.updateNote(id, bo);
        return R.ok();
    }

    /**
     * 标记为已掌握
     *
     * @param id 错题记录ID
     */
    @SaCheckLogin
    @Log(title = "错题本", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/master")
    public R<Void> master(@NotNull(message = "错题ID不能为空") @PathVariable("id") Long id) {
        wrongQuestionService.markMastered(id);
        return R.ok();
    }

    /**
     * 移出错题本（标记已忽略，可在「已忽略」里找回）
     *
     * @param id 错题记录ID
     */
    @SaCheckLogin
    @Log(title = "错题本", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/ignore")
    public R<Void> ignore(@NotNull(message = "错题ID不能为空") @PathVariable("id") Long id) {
        wrongQuestionService.markIgnored(id);
        return R.ok();
    }

    /**
     * 恢复已忽略的错题
     *
     * @param id 错题记录ID
     */
    @SaCheckLogin
    @Log(title = "错题本", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/restore")
    public R<Void> restore(@NotNull(message = "错题ID不能为空") @PathVariable("id") Long id) {
        wrongQuestionService.restore(id);
        return R.ok();
    }

    /**
     * 彻底删除错题
     *
     * @param id 错题记录ID
     */
    @SaCheckLogin
    @Log(title = "错题本", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> remove(@NotNull(message = "错题ID不能为空") @PathVariable("id") Long id) {
        wrongQuestionService.deleteById(id);
        return R.ok();
    }
}
