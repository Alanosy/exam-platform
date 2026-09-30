package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.Grade;
import cn.org.alan.exam.model.vo.stat.GradeExamVO;
import cn.org.alan.exam.model.vo.stat.GradeStudentVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 统计数据 Mapper 接口
 *
 * @ Author JinXi
 * @ Version 1.0
 * @ Date 2024/5/12 14:56
 */
public interface StatMapper extends BaseMapper<Grade> {

    /**
     * 统计各班人数
     *
     * @return 人数总数
     */
    List<GradeStudentVO> StudentGradeCount(@Param("roleId") Integer roleId, Integer id, List<Integer> gradeIdList);

    /**
     * 统计各班试卷数
     *
     * @return 试卷总数
     */
    List<GradeExamVO> ExamGradeCount(@Param("roleId") Integer roleId, Integer id, List<Integer> gradeIdList);

}
