package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.QuestionConverter;
import cn.org.alan.exam.mapper.ExerciseRecordMapper;
import cn.org.alan.exam.mapper.QuestionMapper;
import cn.org.alan.exam.model.entity.ExerciseRecord;
import cn.org.alan.exam.model.entity.Question;
import cn.org.alan.exam.model.vo.exercise.QuestionSheetVO;
import cn.org.alan.exam.service.IExerciseRecordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Service
public class ExerciseRecordServiceImpl extends ServiceImpl<ExerciseRecordMapper, ExerciseRecord>
        implements IExerciseRecordService {

    @Resource
    private QuestionMapper questionMapper;
    @Resource
    private QuestionConverter questionConverter;

    @Override
    public Result<List<QuestionSheetVO>> getQuestionSheet(Integer repoId, Integer quType) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<Question>()
                .select(Question::getId, Question::getQuType, Question::getRepoId)
                .eq(Question::getRepoId, repoId).eq(quType != null && quType != 0, Question::getQuType, quType);

        List<QuestionSheetVO> list = questionConverter.listEntityToVO(questionMapper.selectList(wrapper));

        return Result.success(null, list);
    }
}
