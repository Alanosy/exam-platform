package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.ExamGrade;
import cn.org.alan.exam.model.vo.stat.AllStatsVO;
import cn.org.alan.exam.model.vo.stat.DailyVO;
import cn.org.alan.exam.model.vo.stat.GradeExamVO;
import cn.org.alan.exam.model.vo.stat.GradeStudentVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 统计管理服务接口
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface IStatService extends IService<ExamGrade> {

    /**
     * 各班人数统计
     *
     * @return 响应结果
     */
    Result<List<GradeStudentVO>> getStudentGradeCount();

    /**
     * 各班级的试卷数统计
     *
     * @return 响应结果
     */
    Result<List<GradeExamVO>> getExamGradeCount();

    /**
     * 统计所有班级、试卷、试题数量
     *
     * @return 响应结果
     */
    Result<AllStatsVO> getAllCount();

    /**
     * 获取用户登录时间统计
     *
     * @return
     */
    Result<List<DailyVO>> getDaily();
}
