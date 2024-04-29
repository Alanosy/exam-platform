package cn.org.alan.exam.converter;

import cn.org.alan.exam.model.entity.Exam;
import cn.org.alan.exam.model.entity.ExamQuestion;
import cn.org.alan.exam.model.form.exam.ExamAddForm;
import cn.org.alan.exam.model.form.exam.ExamUpdateForm;
import cn.org.alan.exam.model.vo.exam.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author Alan
 * @Version
 * @Date 2024/4/1 3:18 PM
 */
@Component
@Mapper(componentModel="spring")
public interface ExamConverter {
    Page<ExamVO> pageEntityToVo(Page<Exam> examPage);
    Exam  formToEntity(ExamUpdateForm examUpdateForm);
    Exam  formToEntity(ExamAddForm examAddForm);
    List<ExamDetailRespVO> listEntityToExamDetailRespVO(List<ExamQuestion> examQuestion);
    ExamDetailVO examToExamDetailVO(Exam exam);
}
