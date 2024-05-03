package cn.org.alan.exam.converter;

import cn.org.alan.exam.model.entity.Grade;
import cn.org.alan.exam.model.form.GradeForm;
import cn.org.alan.exam.model.vo.GradeVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author Alan
 * @Version
 * @Date 2024/3/28 2:03 PM
 */
@Component
@Mapper(componentModel="spring")
public interface GradeConverter {

    Page<GradeVO> pageEntityToVo(Page<Grade> page);

    Grade formToEntity(GradeForm gradeForm);

    List<GradeVO> listEntityToVo(List<Grade> page);

}
