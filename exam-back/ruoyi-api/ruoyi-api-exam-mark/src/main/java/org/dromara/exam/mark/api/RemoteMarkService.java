package org.dromara.exam.mark.api;

import org.dromara.exam.mark.api.domain.RemoteMarkSyncBo;

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
}
