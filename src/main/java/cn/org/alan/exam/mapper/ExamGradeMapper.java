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
 * 考试与班级关联表 Mapper 接口
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface ExamGradeMapper extends BaseMapper<ExamGrade> {

    /**
     * 添加试卷与班级的关联
     *
     * @param examId   试卷ID
     * @param gradeIds 班级ID集合
     * @return 添加记录数
     */
    Integer addExamGrade(Integer examId, List<Integer> gradeIds);

    /**
     * 根据开始id获取所有需要参加该考试的人数
     *
     * @param id 考试id
     * @return 人数
     */
    Integer selectClassSize(Integer id);

    /**
     * 查询考试班级关联列表
     *
     * @param examPage 分页page对象
     * @param userId   用户ID
     * @param title    标题
     * @param isASC    是否排序
     * @return 结果
     */
    IPage<ExamGradeListVO> selectClassExam(IPage<ExamGradeListVO> examPage, Integer userId, String title, Boolean isASC);

    /**
     * 获取管理员的试卷列表
     *
     * @param examPage 分页page对象
     * @param userId   用户ID
     * @param title    标题
     * @param isASC    是否排序
     * @return 结果
     */
    IPage<ExamGradeListVO> selectAdminClassExam(IPage<ExamGradeListVO> examPage, Integer userId, String title, Boolean isASC);

}
