package cn.org.alan.exam.converter;

import cn.org.alan.exam.model.entity.ExamQuAnswer;
import cn.org.alan.exam.model.entity.Notice;
import cn.org.alan.exam.model.form.NoticeForm;
import cn.org.alan.exam.model.form.examquanswer.ExamQuAnswerAddForm;
import cn.org.alan.exam.model.vo.NoticeVO;
import cn.org.alan.exam.model.vo.exam.ExamQuAnswerVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

/**
 * @Author Alan
 * @Version
 * @Date 2024/5/6 10:54 AM
 */
@Component
@Mapper(componentModel="spring")
public interface ExamQuAnswerConverter {

    @Mapping(target = "questionId",source = "quId")
    ExamQuAnswer formToEntity(ExamQuAnswerAddForm examQuAnswerAddForm);

}
