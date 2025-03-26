package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.UserExamsScore;
import cn.org.alan.exam.model.vo.answer.UncorrectedUserVO;
import cn.org.alan.exam.model.vo.score.ExportScoreVO;
import cn.org.alan.exam.model.vo.score.GradeScoreVO;
import cn.org.alan.exam.model.vo.score.UserScoreVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * 试卷分数表 Mapper 接口
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface UserExamsScoreMapper extends BaseMapper<UserExamsScore> {

    /**
     * 考试班级用户成绩分析
     *
     * @param page        分页信息
     * @param gradeId     班级Id
     * @param examTitle   考试名称
     * @param userId      用户Id
     * @param roleId      角色Id
     * @param gradeIdList 班级ID集合
     * @return 结果
     */
    IPage<GradeScoreVO> scoreStatistics(IPage<GradeScoreVO> page, Integer gradeId, String examTitle, Integer userId,
                                        Integer roleId, List<Integer> gradeIdList);

    /**
     * 成绩分页查询
     *
     * @param page     分页信息
     * @param gradeId  班级Id
     * @param examId   考试Id
     * @param realName 真实姓名
     * @return 查询结果集
     */
    IPage<UserScoreVO> pagingScore(IPage<UserScoreVO> page, Integer gradeId, Integer examId, String realName);

    /**
     * 获取成绩
     *
     * @param examId  考试id
     * @param gradeId 班级id
     * @return 查询结果
     */
    List<ExportScoreVO> selectScores(Integer examId, Integer gradeId);

    /**
     * 根据考试id获取未考试用户
     *
     * @param page   分页信息
     * @param examId 考试id
     * @return 查询结果
     */
    IPage<UncorrectedUserVO> uncorrectedUser(IPage<UncorrectedUserVO> page, Integer examId, String realName);

}
