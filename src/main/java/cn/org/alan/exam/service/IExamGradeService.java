package cn.org.alan.exam.service;

import cn.org.alan.exam.model.entity.Exam;
import cn.org.alan.exam.model.entity.ExamGrade;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface IExamGradeService extends IService<ExamGrade> {

    List<Exam> getExamGradeCount(int roleId);
}
