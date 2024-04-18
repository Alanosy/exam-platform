package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.Exam;
import cn.org.alan.exam.model.entity.ExamGrade;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface ExamGradeMapper extends BaseMapper<ExamGrade> {

    Integer addExamGrade(Integer id, List<Integer> gradeIds);

    Integer delExamGrade(Integer id);

    //各班试卷统计信息   添加一个自定义的ExamGradeCount统计方法接受教师角色ID作为参数，返回包含各班试卷统计信息的PaperStatistics列表。具体的SQL查询逻辑将在对应的Mapper XML文件（如ExamGradeMapper.xml）中实现。
    List<Exam> ExamGradeCount(@Param("roleId") Integer roleId);

}
