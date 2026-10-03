package org.dromara.exam.question.api;

import org.dromara.exam.question.api.domain.RemoteQuestionBankVo;
import org.dromara.exam.question.api.domain.RemoteQuestionSaveBo;
import org.dromara.exam.question.api.domain.RemoteQuestionSearchBo;
import org.dromara.exam.question.api.domain.RemoteQuestionVo;

import java.util.Collection;
import java.util.List;

/**
 * 试题服务（跨服务调用）
 *
 * <p>除了答题侧「取题目」的读接口，这里还承担 AI 对话场景的两件事：
 * 定位题库（模糊匹配）与批量写入 AI 生成的试题。
 * 写操作放在题库服务里做，是为了保证「试题只有一处写入口」，
 * 选项、知识点关联、状态兜底这些规则不会在调用方各写一份。
 *
 * @author LionLi
 * @date 2026-09-30
 */
public interface RemoteQuestionService {

    /**
     * 按试题ID批量查询题目（含选项与参考答案）
     *
     * @param questionIds 试题ID集合
     * @return 题目列表，按传入顺序返回
     */
    List<RemoteQuestionVo> listByIds(Collection<Long> questionIds);

    /**
     * 按名称关键词模糊查询题库
     *
     * <p>关键词为空时返回全部（受 limit 限制）。返回里带题目数量，
     * 便于调用方在「多个命中」时给出更有信息量的选择列表。
     *
     * @param keyword  题库名称关键词，为空查全部
     * @param limit    返回条数上限，小于 1 按 20 算
     * @param tenantId 租户ID，Dubbo 调用没有登录上下文，必须显式传入
     * @return 题库列表，永远非空（无数据时为空 List）
     */
    List<RemoteQuestionBankVo> listBanks(String keyword, Integer limit, String tenantId);

    /**
     * 新建题库
     *
     * @param bankName 题库名称
     * @param tenantId 租户ID，Dubbo 调用没有登录上下文，必须显式传入
     * @return 新建题库ID，失败返回 null
     */
    Long createBank(String bankName, String tenantId);

    /**
     * 按条件检索试题
     *
     * @param bo 检索条件
     * @return 试题列表（含选项），没有时为空 List
     */
    List<RemoteQuestionVo> searchQuestions(RemoteQuestionSearchBo bo);

    /**
     * 批量保存试题（含选项与知识点关联）
     *
     * <p>逐题落库，单题失败不影响其它题；返回成功写入的试题ID，
     * 调用方按返回数量判断是否有失败。
     *
     * @param bo 保存入参
     * @return 新建试题的ID列表，按入参顺序
     */
    List<Long> saveQuestions(RemoteQuestionSaveBo bo);
}
