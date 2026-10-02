package org.dromara.exam.mark.api;

import org.dromara.exam.mark.api.domain.RemoteMarkSyncBo;

import java.util.List;

/**
 * 阅卷服务（跨服务调用）
 *
 * <p>答题服务交卷后把主观题作答推过来建阅卷任务，
 * 之后教师阅卷、回写成绩、错题入错题本都在阅卷服务内部闭环。
 *
 * @author ruoyi
 * @date 2026-10-01
 */
public interface RemoteMarkService {

    /**
     * 同步阅卷任务：客观题已自动判分，这里只收主观题
     *
     * <p>同一份答卷重复同步时按 recordId 幂等：已有任务就只补齐缺失的题目，
     * 不会把教师已经打过的分冲掉。
     *
     * @param bo 待阅主观题清单，没有主观题时直接返回
     * @return 新建的阅卷任务ID，没有主观题时返回 null
     */
    Long syncSubjective(RemoteMarkSyncBo bo);

    /**
     * 某场考试还有哪些答卷没阅完（pending + marking），拿到的是答卷ID
     *
     * <p>考试管理端用它做两件事：列表 size 就是「待阅份数」（决定要不要给「去阅卷」
     * 的入口），逐个 contains 就是「这个考生的主观题阅完没有」（决定这一行显示
     * 「待阅」还是显示成绩）。一次调用够用，不要为这两个数各建一个接口。
     *
     * @param examId 考试ID
     * @return 未阅完的答卷ID，考试没有待阅内容时返回空列表；查询失败同样返回空列表
     */
    List<Long> listPendingRecordIds(Long examId);
}
