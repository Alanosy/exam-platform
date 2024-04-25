package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.UserExamsScore;
import cn.org.alan.exam.model.vo.score.UserScoreVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface IUserExamsScoreService extends IService<UserExamsScore> {

    /**
     * 分页获取成绩信息
     * @param pageNum 页码
     * @param pageSize 每页记录数
     * @param gradeId 班级Id
     * @param examId 考试Id
     * @param realName 真实姓名
     * @return 查询结果集
     */
    Result<IPage<UserScoreVO>> pagingScore(Integer pageNum, Integer pageSize, Integer gradeId, Integer examId, String realName);

    /**
     * 成绩导出
     * @param response 响应对象
     * @param examId 考试id
     * @param gradeId 班级id
     */
    void exportScores(HttpServletResponse response,Integer examId, Integer gradeId);
}
