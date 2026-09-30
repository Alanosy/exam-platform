package org.dromara.exam.question.api;

import org.dromara.exam.question.api.domain.RemoteQuestionVo;

import java.util.Collection;
import java.util.List;

/**
 * 试题服务（跨服务调用）
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

}
