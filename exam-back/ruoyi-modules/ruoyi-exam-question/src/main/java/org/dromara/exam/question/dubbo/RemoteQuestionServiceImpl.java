package org.dromara.exam.question.dubbo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.exam.question.api.RemoteQuestionService;
import org.dromara.exam.question.api.domain.RemoteQuestionOptionVo;
import org.dromara.exam.question.api.domain.RemoteQuestionVo;
import org.dromara.exam.question.domain.Question;
import org.dromara.exam.question.domain.QuestionOption;
import org.dromara.exam.question.mapper.QuestionMapper;
import org.dromara.exam.question.mapper.QuestionOptionMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 试题服务对外实现（供答题服务取题目与判分）
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DubboService
public class RemoteQuestionServiceImpl implements RemoteQuestionService {

    private final QuestionMapper questionMapper;

    private final QuestionOptionMapper questionOptionMapper;

    /**
     * 按试题ID批量查询题目，顺序与入参保持一致
     */
    @Override
    public List<RemoteQuestionVo> listByIds(Collection<Long> questionIds) {
        if (CollUtil.isEmpty(questionIds)) {
            return List.of();
        }
        List<Long> ids = questionIds.stream().filter(id -> id != null).distinct().toList();
        if (CollUtil.isEmpty(ids)) {
            return List.of();
        }
        List<Question> questions = questionMapper.selectList(
            Wrappers.lambdaQuery(Question.class).in(Question::getId, ids));
        if (CollUtil.isEmpty(questions)) {
            return List.of();
        }
        // 一次查回所有选项，避免在循环里逐题查库
        List<QuestionOption> options = questionOptionMapper.selectList(
            Wrappers.lambdaQuery(QuestionOption.class)
                .in(QuestionOption::getQuestionId, ids)
                .orderByAsc(QuestionOption::getSort)
                .orderByAsc(QuestionOption::getId));
        Map<Long, List<QuestionOption>> optionMap = options.stream()
            .collect(Collectors.groupingBy(QuestionOption::getQuestionId));

        List<RemoteQuestionVo> vos = new ArrayList<>(questions.size());
        for (Question question : questions) {
            RemoteQuestionVo vo = MapstructUtils.convert(question, RemoteQuestionVo.class);
            if (vo == null) {
                continue;
            }
            vo.setQuestionId(question.getId());
            List<QuestionOption> own = optionMap.get(question.getId());
            if (CollUtil.isNotEmpty(own)) {
                List<RemoteQuestionOptionVo> optionVos = own.stream()
                    .map(item -> {
                        RemoteQuestionOptionVo optionVo = new RemoteQuestionOptionVo();
                        optionVo.setOptionKey(item.getOptionKey());
                        optionVo.setOptionContent(item.getOptionContent());
                        optionVo.setSort(item.getSort());
                        return optionVo;
                    })
                    .sorted(Comparator.comparing(RemoteQuestionOptionVo::getSort, Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();
                vo.setOptions(optionVos);
            }
            vos.add(vo);
        }
        // 按传入顺序还原，方便答题页直接按试卷顺序渲染
        Map<Long, RemoteQuestionVo> voMap = vos.stream().collect(Collectors.toMap(RemoteQuestionVo::getQuestionId, item -> item, (a, b) -> a));
        return ids.stream().map(voMap::get).filter(item -> item != null).toList();
    }
}
