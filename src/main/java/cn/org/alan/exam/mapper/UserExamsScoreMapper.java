package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.UserExamsScore;
import cn.org.alan.exam.model.vo.score.ExportScoreVO;
import cn.org.alan.exam.model.vo.score.GradeScoreVO;
import cn.org.alan.exam.model.vo.score.UserScoreVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface UserExamsScoreMapper extends BaseMapper<UserExamsScore> {

    /**
     * 根据用户Id列表，考试Id获取该考试的最高分、最低分、平均分
     * @param userIds 用户Id列表
     * @param examId 考试Id
     * @param passedScore 及格分
     * @return 结果
     */
    GradeScoreVO scoreStatistics(List<Integer> userIds, Integer examId, Integer passedScore);

    /**
     * 成绩分页查询
     * @param page 分页信息
     * @param gradeId 班级Id
     * @param examId 考试Id
     * @param realName 真实姓名
     * @return 查询结果集
     */
    IPage<UserScoreVO> pagingScore(IPage<UserScoreVO> page, Integer gradeId, Integer examId, String realName);

    Integer getNumberOfApplicants(Integer examId);

    Integer getCorrectedPaper(Integer examId);

    /**
     * 获取成绩
     * @param examId 考试id
     * @param gradeId 班级id
     * @return 查询结果
     */
    List<ExportScoreVO> selectScores(Integer examId, Integer gradeId);
}
