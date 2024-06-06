package cn.org.alan.exam.converter;

import cn.org.alan.exam.model.entity.ExerciseRecord;
import cn.org.alan.exam.model.form.ExerciseFillAnswerFrom;
import cn.org.alan.exam.model.vo.QuestionVO;
import cn.org.alan.exam.model.vo.exercise.AnswerInfoVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.stereotype.Component;

/**
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/5/6 10:15
 */
@Component
@Mapper(componentModel="spring")
public interface ExerciseConverter {
    @Mappings({
            @Mapping(source = "quId",target = "questionId"),
            @Mapping(source = "quType",target = "questionType")
    })
    ExerciseRecord fromToEntity(ExerciseFillAnswerFrom exerciseFillAnswerFrom);

    AnswerInfoVO quVOToAnswerInfoVO(QuestionVO questionVO);
}
