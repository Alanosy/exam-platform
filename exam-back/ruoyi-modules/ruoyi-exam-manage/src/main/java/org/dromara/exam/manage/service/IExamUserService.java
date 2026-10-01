package org.dromara.exam.manage.service;

import org.dromara.exam.manage.domain.vo.ExamWhiteUserVo;
import org.dromara.exam.manage.domain.bo.ExamUserBo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 考试白名单考生Service接口
 *
 * <p>白名单模式下，考试apriori只允许这里的考生参加。
 *
 * @author LionLi
 * @date 2026-10-02
 */
public interface IExamUserService {

    /**
     * 查询某场考试的白名单考生（含昵称 / 部门名）
     *
     * @param examId 考试ID
     * @return 白名单考生列表，没有时返回空List
     */
    List<ExamWhiteUserVo> queryWhiteUsers(Long examId);

    /**
     * 查询某场考试的白名单考生ID
     *
     * @param examId 考试ID
     * @return 考生用户ID列表，没有时返回空List
     */
    List<Long> queryWhiteUserIds(Long examId);

    /**
     * 整体覆盖某场考试的白名单
     *
     * @param bo 考试ID + 考生用户ID列表，不在列表里的原有考生会被移出
     */
    void saveWhiteUsers(ExamUserBo bo);

    /**
     * 批量统计考试的应考人数
     *
     * @param examIds 考试ID列表
     * @return key = 考试ID，value = 白名单人数
     */
    Map<Long, Long> countMapByExamIds(List<Long> examIds);

    /**
     * 按考试删除白名单（考试被删时清掉孤儿名单）
     *
     * @param examIds 考试ID列表
     */
    void deleteByExamIds(Collection<Long> examIds);

}
