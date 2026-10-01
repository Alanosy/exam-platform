package org.dromara.exam.answer.api;

import org.dromara.exam.answer.api.domain.RemoteAnswerVo;
import org.dromara.exam.answer.api.domain.RemoteMarkWriteBackBo;
import org.dromara.exam.answer.api.domain.RemoteRecordVo;

import java.util.List;

/**
 * 答卷服务（跨服务调用）
 *
 * <p>答卷与成绩都在 ry-exam-answer 库，阅卷服务不直连那个库，
 * 全部通过这个接口读写，保证「成绩只有一处写入口」。
 *
 * @author ruoyi
 * @date 2026-10-01
 */
public interface RemoteExamAnswerService {

    /**
     * 查询答卷记录
     *
     * @param recordId 答卷记录ID
     * @return 答卷记录，不存在时返回 null
     */
    RemoteRecordVo queryRecord(Long recordId);

    /**
     * 查询某场考试下的答卷记录
     *
     * @param examId 考试ID
     * @param status 状态，为空表示全部
     * @return 答卷记录列表，按交卷时间倒序
     */
    List<RemoteRecordVo> listRecordsByExam(Long examId, String status);

    /**
     * 查询某份答卷的逐题作答
     *
     * @param recordId 答卷记录ID
     * @return 作答明细，按题号升序
     */
    List<RemoteAnswerVo> listAnswers(Long recordId);

    /**
     * 查有答卷的考试ID列表
     *
     * <p>阅卷任务表按考试聚合分页时，考试名 / 考试类型都在考试库，
     * 这里先把考试ID取回来再下推成 in 条件，分页条数才不会失真。
     *
     * @return 考试ID列表，没有时返回空List
     */
    List<Long> listExamIdsHasRecord();

    /**
     * 回写阅卷结果
     *
     * <p>更新每道主观题的得分与对错，并重算主观题分；
     * finished 为 true 时再重算总分与及格，避免阅到一半就给出最终成绩。
     *
     * @param bo 阅卷结果
     */
    void writeBackMark(RemoteMarkWriteBackBo bo);
}
