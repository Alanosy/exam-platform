package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.ExamGrade;
import cn.org.alan.exam.model.vo.score.GradeScoreVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface IExamGradeService extends IService<ExamGrade> {

    /**
     * 根据班级Id和考试Id获取该班级的平均分、最高分、最低分以及及格率
     * @param examId 考试Id
     * @param classId 班级Id
     * @return 响应结果
     */
    Result<GradeScoreVO> gradeExamScore(Integer examId, Integer classId);
}
