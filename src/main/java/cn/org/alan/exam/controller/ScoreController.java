package cn.org.alan.exam.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.vo.score.GradeScoreVO;
import cn.org.alan.exam.model.vo.score.UserScoreVO;
import cn.org.alan.exam.service.IExamGradeService;
import cn.org.alan.exam.service.IUserExamsScoreService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 成绩管理
 * </p>
 *
 * @Author WeiJin
 * @Version
 * @Date 2024/3/25 11:19 AM
 */
@RestController
@RequestMapping("/score")
public class ScoreController {

    @Resource
    private IExamGradeService iExamGradeService;
    @Resource
    private IUserExamsScoreService iUserExamsScoreService;

    /**
     * 根据班级Id和考试Id获取该班级的平均分、最高分、最低分以及及格率
     *
     * @param examId  考试Id
     * @param classId 班级Id
     * @return 响应结果
     */
    @GetMapping("/class/{examId}/{classId}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<GradeScoreVO> gradeExamScore(@PathVariable("examId") Integer examId, @PathVariable("classId") Integer classId) {
        return iExamGradeService.gradeExamScore(examId, classId);
    }

    /**
     * 分页获取成绩信息
     * @param pageNum 页码
     * @param pageSize 每页记录数
     * @param gradeId 班级Id
     * @param examId 考试Id
     * @param realName 真实姓名
     * @return 响应结果
     */
    @GetMapping("/paging")
    public Result<IPage<UserScoreVO>> pagingScore(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                                  @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                                  @RequestParam(value = "gradeId", required = false) Integer gradeId,
                                                  @RequestParam(value = "examId", required = false) Integer examId,
                                                  @RequestParam(value = "realName", required = false) String realName) {

        return iUserExamsScoreService.pagingScore(pageNum,pageSize,gradeId,examId,realName);
    }

}
