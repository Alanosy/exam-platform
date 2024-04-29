package cn.org.alan.exam.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.answer.AnswerUpdate;
import cn.org.alan.exam.model.vo.answer.AnswerExamVO;
import cn.org.alan.exam.model.vo.answer.UncorrectedUserVO;
import cn.org.alan.exam.service.IManualScoreService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 答题管理
 *
 * @Author WeiJin
 * @Version
 * @Date 2024/3/25 11:20 AM
 */
@RestController
@RequestMapping("/api/answers")
public class AnswerController {
    @Resource
    private IManualScoreService manualScoreService;

    /**
     * 试卷查询信息
     *
     * @return
     */
    @GetMapping("/detail")
    public Result getDetail(@RequestParam Integer userId,
                            @RequestParam Integer examId) {
        return manualScoreService.getDetail(userId, examId);
    }


    /**
     * 批改试卷
     *
     * @return
     */
    @PutMapping("/correct")
    public Result Correct(@RequestBody AnswerUpdate answerUpdate) {
        return manualScoreService.correct(answerUpdate);
    }

    /**
     * 分页查找待阅卷考试
     *
     * @return
     */
    @GetMapping("/exam/page")
    public Result<IPage<AnswerExamVO>> examPage(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                                @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        return manualScoreService.examPage(pageNum, pageSize);
    }

    /**
     * 查询考试的用户
     *
     * @param pageNum
     * @param pageSize
     * @param examId
     * @return
     */
    @GetMapping("/exam/stu")
    public Result<IPage<UncorrectedUserVO>> stuExamPage(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                                        @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                                        @RequestParam(value = "examId") Integer examId) {
        return manualScoreService.stuExamPage(pageNum, pageSize, examId);
    }
}
