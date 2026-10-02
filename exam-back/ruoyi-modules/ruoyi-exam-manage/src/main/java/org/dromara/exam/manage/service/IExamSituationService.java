package org.dromara.exam.manage.service;

import jakarta.servlet.http.HttpServletResponse;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.exam.manage.domain.bo.ExamSituationBo;
import org.dromara.exam.manage.domain.vo.ExamSituationOverviewVo;
import org.dromara.exam.manage.domain.vo.ExamSituationVo;

import java.util.List;

/**
 * 考试情况Service接口
 *
 * <p>给考试管理列表的「考试情况」页供数：谁参考了、考得怎么样、导出、有多少主观题没阅。
 *
 * <p>数据实时取，不走汇总表——汇总表要等统计任务跑过才有数，管理端打开就得看得见。
 *
 * @author ruoyi
 * @date 2026-10-02
 */
public interface IExamSituationService {

    /**
     * 整场考试的概览：应考 / 参考 / 已交卷 / 待阅 / 平均分 / 及格率
     *
     * @param examId 考试ID
     * @return 概览，考试不存在时返回只有基本信息的空壳
     */
    ExamSituationOverviewVo overview(Long examId);

    /**
     * 参考名单与成绩
     *
     * @param bo        过滤条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    TableDataInfo<ExamSituationVo> listSituation(ExamSituationBo bo, PageQuery pageQuery);

    /**
     * 导出整场考试情况
     *
     * @param bo       过滤条件，与页面上的筛选保持一致
     * @param response 响应流
     */
    void export(ExamSituationBo bo, HttpServletResponse response);

    /**
     * 查出参与筛选的全部行（导出用，不受分页限制）
     *
     * @param bo 过滤条件
     * @return 全部行
     */
    List<ExamSituationVo> queryRows(ExamSituationBo bo);
}
