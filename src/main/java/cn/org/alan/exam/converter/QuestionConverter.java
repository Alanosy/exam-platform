package cn.org.alan.exam.converter;

import cn.org.alan.exam.model.entity.Question;
import cn.org.alan.exam.model.form.question.QuestionFrom;
import cn.org.alan.exam.model.vo.exercise.QuestionSheetVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/4/1 15:46
 */
@Component
@Mapper(componentModel = "spring")
public interface QuestionConverter {

    @Mapping(target = "repoId",source = "repoId")
    Question fromToEntity(QuestionFrom questionFrom);

    List<QuestionSheetVO> listEntityToVO(List<Question> questions);

}
