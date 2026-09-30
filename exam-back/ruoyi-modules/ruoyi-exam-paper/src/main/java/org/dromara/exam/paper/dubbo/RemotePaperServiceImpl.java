package org.dromara.exam.paper.dubbo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.exam.paper.api.RemotePaperService;
import org.dromara.exam.paper.api.domain.RemotePaperQuestionVo;
import org.dromara.exam.paper.api.domain.RemotePaperVo;
import org.dromara.exam.paper.domain.Paper;
import org.dromara.exam.paper.domain.PaperQuestion;
import org.dromara.exam.paper.mapper.PaperMapper;
import org.dromara.exam.paper.mapper.PaperQuestionMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 试卷服务对外实现（供答题服务开考下发试卷）
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DubboService
public class RemotePaperServiceImpl implements RemotePaperService {

    private final PaperMapper paperMapper;

    private final PaperQuestionMapper paperQuestionMapper;

    /**
     * 查询试卷信息
     */
    @Override
    public RemotePaperVo queryPaper(Long paperId) {
        if (ObjectUtil.isNull(paperId)) {
            return null;
        }
        Paper paper = paperMapper.selectById(paperId);
        if (ObjectUtil.isNull(paper)) {
            return null;
        }
        RemotePaperVo vo = MapstructUtils.convert(paper, RemotePaperVo.class);
        if (ObjectUtil.isNull(vo)) {
            return null;
        }
        vo.setPaperId(paper.getId());
        return vo;
    }

    /**
     * 查询试卷内的题目清单，按 sort 升序
     */
    @Override
    public List<RemotePaperQuestionVo> listQuestions(Long paperId) {
        if (ObjectUtil.isNull(paperId)) {
            return List.of();
        }
        List<PaperQuestion> list = paperQuestionMapper.selectList(
            Wrappers.lambdaQuery(PaperQuestion.class)
                .eq(PaperQuestion::getPaperId, paperId)
                .orderByAsc(PaperQuestion::getSort)
                .orderByAsc(PaperQuestion::getId));
        if (CollUtil.isEmpty(list)) {
            return List.of();
        }
        List<RemotePaperQuestionVo> vos = new ArrayList<>(list.size());
        for (PaperQuestion item : list) {
            RemotePaperQuestionVo vo = new RemotePaperQuestionVo();
            vo.setQuestionId(item.getQuestionId());
            vo.setScore(item.getPaperScore());
            vo.setSort(ObjectUtil.isNotNull(item.getSort()) ? item.getSort() : (long) vos.size() + 1);
            vos.add(vo);
        }
        vos.sort(Comparator.comparing(RemotePaperQuestionVo::getSort));
        return vos;
    }
}
