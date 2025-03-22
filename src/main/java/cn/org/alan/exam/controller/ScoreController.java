package cn.org.alan.exam.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.vo.score.GradeScoreVO;
import cn.org.alan.exam.model.vo.score.QuestionAnalyseVO;
import cn.org.alan.exam.model.vo.score.UserScoreVO;
import cn.org.alan.exam.service.IStatService;
import cn.org.alan.exam.service.IExamQuAnswerService;
import cn.org.alan.exam.service.IUserExamsScoreService;
import com.baomidou.mybatisplus.core.metadata.IPage;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 成绩管理
 *
 * @Author WeiJin
 * @Version
 * @Date 2024/3/25 11:19 AM
 */
@Api(tags = "成绩相关接口")
@RestController
@RequestMapping("/api/score")
public class ScoreController {

    @Resource
    private IUserExamsScoreService iUserExamsScoreService;
    @Resource
    private IExamQuAnswerService iExamQuAnswerService;

    /**
     * 分页获取成绩信息
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param gradeId  班级Id
     * @param examId   考试Id
     * @param realName 真实姓名
     * @return 响应结果
     */
    @ApiOperation("分页获取成绩信息")
    @GetMapping("/paging")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<IPage<UserScoreVO>> pagingScore(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                                  @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                                  @RequestParam(value = "gradeId") Integer gradeId,
                                                  @RequestParam(value = "examId") Integer examId,
                                                  @RequestParam(value = "realName", required = false) String realName) {
        return iUserExamsScoreService.pagingScore(pageNum, pageSize, gradeId, examId, realName);
    }

    /**
     * 获取某场考试某题作答情况
     *
     * @param examId     考试id
     * @param questionId 试题id
     * @return 响应结果
     */
    @ApiOperation("获取某场考试某题作答情况")
    @GetMapping("/question/{examId}/{questionId}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<QuestionAnalyseVO> questionAnalyse(@PathVariable("examId") Integer examId,
                                                     @PathVariable("questionId") Integer questionId) {
        return iExamQuAnswerService.questionAnalyse(examId, questionId);
    }

    /**
     * 根据班级分析考试情况
     *
     * @param pageNum   页码
     * @param pageSize  每页记录数
     * @param examTitle 考试名称
     * @return 响应结果
     */
    @ApiOperation("根据班级分析考试情况")
    @GetMapping("/getExamScore")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public Result<IPage<GradeScoreVO>> getExamScoreInfo(
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "examTitle", required = false) String examTitle,
            @RequestParam(value = "gradeId", required = false) Integer gradeId) {
        return iUserExamsScoreService.getExamScoreInfo(pageNum, pageSize, examTitle, gradeId);
    }

    /**
     * 成绩导出
     *
     * @param response 响应对象
     * @param examId   考试id
     * @param gradeId  班级id
     */
    @ApiOperation("成绩导出")
    @GetMapping("/export/{examId}/{gradeId}")
    @PreAuthorize("hasAnyAuthority('role_teacher','role_admin')")
    public void scoreExport(HttpServletResponse response, @PathVariable("examId") Integer examId, @PathVariable("gradeId") Integer gradeId) {
        iUserExamsScoreService.exportScores(response, examId, gradeId);
    }

}
