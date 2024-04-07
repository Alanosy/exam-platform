package cn.org.alan.exam.converter;

import cn.org.alan.exam.model.entity.Exam;
import cn.org.alan.exam.model.form.exam.ExamForm;
import cn.org.alan.exam.model.vo.ExamVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

/**
 * @Author Alan
 * @Version
 * @Date 2024/4/1 3:18 PM
 */
@Component
@Mapper(componentModel="spring")
public interface ExamConverter {
    Page<ExamVO> pageEntityToVo(Page<Exam> examPage);
    Exam  formToEntity(ExamForm examForm);
}
