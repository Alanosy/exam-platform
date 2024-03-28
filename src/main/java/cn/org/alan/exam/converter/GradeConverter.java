package cn.org.alan.exam.converter;

import cn.org.alan.exam.model.entity.Grade;
import cn.org.alan.exam.model.form.GradeForm;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

/**
 * @Author Alan
 * @Version
 * @Date 2024/3/28 2:03 PM
 */
@Component
@Mapper(componentModel="spring")
public interface GradeConverter {
    Grade formEntity(GradeForm gradeForm);
}
