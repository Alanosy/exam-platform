package cn.org.alan.exam.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.answer.AnswerUpdate;
import cn.org.alan.exam.model.vo.answer.AnswerExamPageVO;
import cn.org.alan.exam.model.vo.answer.AnswerPageVO;
import cn.org.alan.exam.service.IExamQuAnswerService;
import cn.org.alan.exam.service.IManualScoreService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 *     答题管理
 * @Author Alan
 * @Version
 * @Date 2024/3/25 11:20 AM
 */
@RestController
@RequestMapping("/answers")
public class AnswerController {
    @Resource
    private IManualScoreService manualScoreService;

    /**
     * 试卷查询信息
     * @return
     */
    @GetMapping("/detail")
    public Result getDetail(@RequestParam Integer userId,
                            @RequestParam Integer examId){
        return manualScoreService.getDetail(userId,examId);
    }

    /**
     * 分页查询答题列表
     * @return
     */
    @GetMapping("/paging")
    public Result<IPage<AnswerPageVO>> getPaging(@RequestParam(value = "pageNum",required = false, defaultValue = "1") Integer pageNum,
                                                 @RequestParam(value = "pageSize",required = false, defaultValue = "10") Integer pageSize){
        return manualScoreService.getPaging(pageNum,pageSize);
    }

    /**
     * 批改试卷
     * @return
     */
    @PutMapping("/correct")
    public Result Correct(@RequestBody AnswerUpdate answerUpdate){
        return manualScoreService.Correct(answerUpdate);
    }

    /**
     * 分页查找待阅卷考试
     * @return
     */
    @GetMapping("/exam/page")
    public Result<IPage<AnswerExamPageVO>> examPage(){
        return manualScoreService.examPage();
    }
}
