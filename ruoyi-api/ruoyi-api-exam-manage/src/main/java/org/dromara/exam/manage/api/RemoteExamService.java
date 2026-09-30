package org.dromara.exam.manage.api;

import org.dromara.exam.manage.api.domain.RemoteExamInviteVo;
import org.dromara.exam.manage.api.domain.RemoteExamVo;

import java.util.List;

/**
 * 考试服务（跨服务调用）
 *
 * <p>答题服务开考前需要校验考试的时间、迟到、重考次数等规则，
 * 以及「这个人被邀请/加入了哪些考试」，这些数据都在考试管理服务里。
 *
 * @author LionLi
 * @date 2026-09-30
 */
public interface RemoteExamService {

    /**
     * 查询考试信息
     *
     * @param examId 考试ID
     * @return 考试信息，不存在时返回 null
     */
    RemoteExamVo queryExam(Long examId);

    /**
     * 按账号查询该账号被邀请 / 已加入的考试
     *
     * @param account 账号（登录名 / 手机号 / 邮箱）
     * @return 邀请记录列表
     */
    List<RemoteExamInviteVo> listInvitesByAccount(String account);

    /**
     * 查询某账号在某场考试上的邀请记录
     *
     * @param examId  考试ID
     * @param account 账号
     * @return 邀请记录，不存在时返回 null
     */
    RemoteExamInviteVo queryInvite(Long examId, String account);

}
