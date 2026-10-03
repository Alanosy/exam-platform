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
     * 查某类任务下的考试ID列表
     *
     * <p>答题记录库里没有冗余考试类型，按类型筛选用「半连接」的方式先把考试ID取回来，
     * 再作为 in 条件下推到 SQL，分页条数才不会失真。
     *
     * @param examType 任务类型：1正式考试 / 2练习考试
     * @return 考试ID列表，没有时返回空List
     */
    List<Long> listExamIdsByType(String examType);

    /**
     * 查某个创建人名下的考试ID列表
     *
     * <p>自己建的考试不用先拿链接加入，答题服务靠这个方法把它直接放进考试中心。
     *
     * @param creatorId 创建人用户ID
     * @return 考试ID列表，没有时返回空List
     */
    List<Long> listExamIdsByCreator(Long creatorId);

    /**
     * 查某个白名单考生被指派的考试ID列表
     *
     * <p>与「加入链接」相对：白名单考生不用也不可能拿到链接，靠这张名单把它放进考试中心。
     *
     * @param userId 考生用户ID
     * @return 考试ID列表，没有时返回空List
     */
    List<Long> listExamIdsByWhiteUser(Long userId);

    /**
     * 判断某人是否在某场考试的白名单里
     *
     * @param examId 考试ID
     * @param userId 考生用户ID
     * @return 在名单里返回 true
     */
    Boolean isWhiteUser(Long examId, Long userId);

    /**
     * 按名称关键词模糊查询考试
     *
     * <p>AI 对话场景用：用户说「某某考卷的答题情况」时，Agent 拿关键词把考试找出来，
     * 唯一命中直接用，多个命中才让人选。
     *
     * @param keyword  考试名称关键词，为空查全部
     * @param limit    返回条数上限，小于 1 按 10 算
     * @param tenantId 租户ID，Dubbo 调用没有登录上下文，必须显式传入
     * @return 考试列表，永远非空（无数据时为空 List）
     */
    List<RemoteExamVo> searchExams(String keyword, Integer limit, String tenantId);

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
