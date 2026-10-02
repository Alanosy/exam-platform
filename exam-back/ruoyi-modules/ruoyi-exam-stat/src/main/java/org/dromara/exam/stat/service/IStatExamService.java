package org.dromara.exam.stat.service;

import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.exam.stat.domain.bo.StatExamQueryBo;
import org.dromara.exam.stat.domain.bo.StatQuestionQueryBo;
import org.dromara.exam.stat.domain.bo.StatUserQueryBo;
import org.dromara.exam.stat.domain.vo.StatAnswerVo;
import org.dromara.exam.stat.domain.vo.StatDashboardVo;
import org.dromara.exam.stat.domain.vo.StatExamRowVo;
import org.dromara.exam.stat.domain.vo.StatKnowledgeVo;
import org.dromara.exam.stat.domain.vo.StatOverviewVo;
import org.dromara.exam.stat.domain.vo.StatQuestionVo;
import org.dromara.exam.stat.domain.vo.StatUserVo;

import java.util.List;

/**
 * 考试统计
 *
 * <p>所有的统计数字都从预计算的汇总表读，不在查询时扫答卷。
 * 汇总表由 {@link #recalc} 全量重算：交卷 / 阅卷完成会置脏，
 * 查询时发现脏了就异步重算一次（实时感），另有定时任务兜底自愈。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
public interface IStatExamService {

    /**
     * 大盘概览
     */
    StatDashboardVo dashboard(StatExamQueryBo bo);

    /**
     * 考试统计列表
     */
    TableDataInfo<StatExamRowVo> listExamPage(StatExamQueryBo bo);

    /**
     * 单场考试详情
     */
    StatOverviewVo overview(Long examId);

    /**
     * 单场考试的分数段分布
     */
    List<org.dromara.exam.stat.domain.vo.StatSegmentVo> listSegment(Long examId);

    /**
     * 考生成绩列表
     */
    TableDataInfo<StatUserVo> listUserPage(StatUserQueryBo bo);

    /**
     * 考生答卷明细
     *
     * @param examId  考试ID
     * @param userId  考生ID
     * @param attemptNo 第几次，为空取最后一次
     */
    StatAnswerVo userDetail(Long examId, Long userId, Integer attemptNo);

    /**
     * 试题分析列表
     */
    TableDataInfo<StatQuestionVo> listQuestionPage(StatQuestionQueryBo bo);

    /**
     * 知识点薄弱分析
     */
    List<StatKnowledgeVo> listKnowledge(Long examId);

    /**
     * 全量重算一场考试
     *
     * @param examId   考试ID
     * @param trigger  触发来源 auto / mark / job / manual
     */
    void recalc(Long examId, String trigger);

    /**
     * 标记 / 取消作废
     */
    void markExcluded(Long examId, Long recordId, boolean excluded);
}
