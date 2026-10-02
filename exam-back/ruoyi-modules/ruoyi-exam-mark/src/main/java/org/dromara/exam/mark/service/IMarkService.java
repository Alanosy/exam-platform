package org.dromara.exam.mark.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.exam.mark.api.domain.RemoteMarkSyncBo;
import org.dromara.exam.mark.domain.bo.MarkExamBo;
import org.dromara.exam.mark.domain.bo.MarkScoreBo;
import org.dromara.exam.mark.domain.bo.MarkTaskBo;
import org.dromara.exam.mark.domain.vo.MarkExamVo;
import org.dromara.exam.mark.domain.vo.MarkLogVo;
import org.dromara.exam.mark.domain.vo.MarkQuestionVo;
import org.dromara.exam.mark.domain.vo.MarkTaskVo;

import java.util.List;

/**
 * 阅卷Service接口
 *
 * @author ruoyi
 * @date 2026-10-01
 */
public interface IMarkService {

    /**
     * 阅卷列表（按考试聚合，只统计正式考试）
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    TableDataInfo<MarkExamVo> listExamPage(MarkExamBo bo, PageQuery pageQuery);

    /**
     * 某场考试下的答卷（阅卷任务）列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分页结果
     */
    TableDataInfo<MarkTaskVo> listTaskPage(MarkTaskBo bo, PageQuery pageQuery);

    /**
     * 阅卷页：某份答卷的主观题明细
     *
     * @param taskId 阅卷任务ID
     * @return 主观题列表，按题号升序
     */
    List<MarkQuestionVo> listQuestions(Long taskId);

    /**
     * 阅卷操作日志
     *
     * @param taskId 阅卷任务ID
     * @return 日志列表，按时间倒序
     */
    List<MarkLogVo> listLogs(Long taskId);

    /**
     * 单题打分
     *
     * @param bo 打分入参
     */
    void score(MarkScoreBo bo);

    /**
     * 确认完成阅卷：全部主观题阅完后回写总分与及格，并把错题推进错题本
     *
     * @param taskId 阅卷任务ID
     */
    void finish(Long taskId);

    /**
     * AI 批量预评：只给建议分与理由，不直接改最终得分，教师确认后才生效
     *
     * @param taskId 阅卷任务ID
     */
    void aiPreview(Long taskId);

    /**
     * 同步阅卷任务（答题服务交卷后调用）
     *
     * @param bo 待阅主观题
     * @return 阅卷任务ID，没有主观题时返回 null
     */
    Long syncSubjective(RemoteMarkSyncBo bo);

    /**
     * 某场考试还有哪些答卷没阅完
     *
     * @param examId 考试ID
     * @return 未阅完的答卷ID
     */
    List<Long> listPendingRecordIds(Long examId);
}
