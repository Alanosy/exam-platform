package org.dromara.exam.practice.api;

import org.dromara.exam.practice.api.domain.RemoteWrongQuestionBo;

import java.util.List;

/**
 * 错题本服务（跨服务调用）
 *
 * <p>错题本数据在考试主库，答题 / 阅卷服务判完分后需要跨库写入，
 * 统一走这个接口，避免业务服务直接操作错题表。
 *
 * @author ruoyi
 * @date 2026-09-30
 */
public interface RemoteWrongQuestionService {

    /**
     * 同步错题（答错或半对时调用）
     *
     * <p>同一个用户同一道题只保留一条记录：已存在则累加错误次数并刷新最近答错时间，
     * 已掌握的题再次答错会退回「未掌握」；不存在则新增。
     *
     * @param boList 错题清单，为空时直接返回
     */
    void syncWrong(List<RemoteWrongQuestionBo> boList);

}
