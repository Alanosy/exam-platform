package org.dromara.exam.answer.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.exam.answer.domain.bo.AnswerSaveBo;
import org.dromara.exam.answer.domain.bo.ExamRecordBo;
import org.dromara.exam.answer.domain.vo.ExamAnswerJudgeVo;
import org.dromara.exam.answer.domain.vo.ExamCenterVo;
import org.dromara.exam.answer.domain.vo.ExamPaperVo;
import org.dromara.exam.answer.domain.vo.ExamRecordVo;
import org.dromara.exam.answer.domain.vo.ExamResultVo;

import java.util.List;

/**
 * 考试答卷Service接口
 *
 * <p>考生侧的完整链路：考试中心列表 → 开考 → 下发试卷 → 逐题作答 → 交卷 → 成绩。
 *
 * @author LionLi
 * @date 2026-09-30
 */
public interface IExamRecordService {

    /**
     * 我的考试列表（考试中心）
     *
     * @return 每个我参与过的考试一张卡片，带「当前该做什么」的状态
     */
    List<ExamCenterVo> listMyCenter();

    /**
     * 我的考试记录（分页）
     *
     * <p>一次答卷一行，带考试名、第几次、用时、得分、及格情况。
     *
     * @param bo        查询条件（考试 / 状态 / 是否及格）
     * @param pageQuery 分页参数
     * @return 考试记录分页
     */
    TableDataInfo<ExamRecordVo> listMyRecords(ExamRecordBo bo, PageQuery pageQuery);

    /**
     * 开始 / 继续考试
     *
     * <p>已有答题中的答卷时直接返回它（中途退出再进来继续答），
     * 否则校验资格、时间、迟到与重考次数后新建一次答卷。
     *
     * @param examId 考试ID
     * @return 答卷记录ID
     */
    Long startExam(Long examId);

    /**
     * 取答题页数据（题目 + 已作答内容 + 剩余时间）
     *
     * @param recordId 答卷记录ID
     * @return 答题页数据
     */
    ExamPaperVo getPaper(Long recordId);

    /**
     * 保存单题作答（可反复调用，覆盖上一次答案）
     *
     * @param recordId 答卷记录ID
     * @param bo       作答内容
     */
    void saveAnswer(Long recordId, AnswerSaveBo bo);

    /**
     * 刷题即时判题：保存本题作答并立刻回判分结果与解析
     *
     * <p>只有开了「即时看答案（immediate）」的练习考试能用；正式考试不给提前判，
     * 避免答案提前泄露。最终成绩仍以交卷时的统一判分为准。
     *
     * @param recordId 答卷记录ID
     * @param bo       作答内容
     * @return 本题判分结果与解析
     */
    ExamAnswerJudgeVo judgeAnswer(Long recordId, AnswerSaveBo bo);

    /**
     * 交卷并判分
     *
     * @param recordId 答卷记录ID
     * @return 成绩
     */
    ExamResultVo submit(Long recordId);

    /**
     * 查询成绩详情
     *
     * @param recordId 答卷记录ID
     * @return 成绩
     */
    ExamResultVo getResult(Long recordId);

    /**
     * 兜底：把所有超时未交卷的答卷自动交卷
     *
     * <p>考生关掉页面不会触发交卷，靠定时任务与下次进入时各兜一次。
     *
     * @return 本次自动交卷的答卷数量
     */
    int autoSubmitExpired();

}
