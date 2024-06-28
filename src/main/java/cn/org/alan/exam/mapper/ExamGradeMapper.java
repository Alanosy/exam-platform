package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.Exam;
import cn.org.alan.exam.model.entity.ExamGrade;
import cn.org.alan.exam.model.vo.exam.ExamGradeListVO;
import cn.org.alan.exam.model.vo.score.GradeScoreVO;
import cn.org.alan.exam.model.vo.stat.GradeExamVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    /**
     * 分页获取考试班级用户信息
     * @param page
     * @param roleId
     * @return
     */
    IPage<GradeScoreVO> getExamGrade(IPage<GradeScoreVO> page,String examTitle,Integer userId,Integer gradeId,Integer roleId);


    /**
     * 根据开始id获取所有需要参加该考试的人数
     * @param id 考试id
     * @return 人数
     */
    Integer selectClassSize(Integer id);


    IPage<ExamGradeListVO> selectClassExam(IPage<ExamGradeListVO> examPage, Integer userId,String title);

    IPage<ExamGradeListVO> selectAdminClassExam(IPage<ExamGradeListVO> examPage, Integer userId,String title);

}
