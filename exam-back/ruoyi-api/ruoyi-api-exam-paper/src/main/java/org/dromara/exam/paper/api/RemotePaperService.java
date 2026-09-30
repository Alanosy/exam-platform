package org.dromara.exam.paper.api;

import org.dromara.exam.paper.api.domain.RemotePaperQuestionVo;
import org.dromara.exam.paper.api.domain.RemotePaperVo;

import java.util.List;

/**
 * 试卷服务（跨服务调用）
 *
 * <p>答题服务开考时按试卷ID取试卷配置与题目清单，
 * 题目正文再由题目服务批量补齐。
 *
 * @author LionLi
 * @date 2026-09-30
 */
public interface RemotePaperService {

    /**
     * 查询试卷信息
     *
     * @param paperId 试卷ID
     * @return 试卷信息，不存在时返回 null
     */
    RemotePaperVo queryPaper(Long paperId);

    /**
     * 查询试卷内的题目清单
     *
     * @param paperId 试卷ID
     * @return 题目清单，按 sort 升序
     */
    List<RemotePaperQuestionVo> listQuestions(Long paperId);

}
