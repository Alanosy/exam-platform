package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.Exam;
import cn.org.alan.exam.model.form.exam.ExamAddForm;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface ExamMapper extends BaseMapper<Exam> {

    int createExam(ExamAddForm examAddForm);

    int deleteExam(List<Integer> examIds);
}
