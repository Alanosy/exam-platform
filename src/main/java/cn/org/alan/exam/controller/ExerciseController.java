package cn.org.alan.exam.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.vo.exercise.QuestionSheetVO;
import cn.org.alan.exam.service.IExerciseRecordService;
import jakarta.annotation.Nullable;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 刷题管理
 *
 * @Author Alan
 * @Version
 * @Date 2024/3/25 11:21 AM
 */
@RestController
@RequestMapping("/api/exercises")
@Validated
public class ExerciseController {

    @Resource
    private IExerciseRecordService iExerciseRecordService;

    /**
     * 获取试题Id列表
     * @param repoId
     * @param quType
     * @return
     */
    @GetMapping("/{repoId}")
    @PreAuthorize("hasAnyAuthority('role_student','role_teacher','role_admin')")
    public Result<List<QuestionSheetVO>> getQuestion(@PathVariable("repoId") Integer repoId,
                                                     @Min(value = 1, message = "试题类型最小值应为1")
                                                     @Max(value = 4, message = "试题类型最大值应为4")
                                                     @Nullable
                                                     @RequestParam(value = "quType", required = false) Integer quType){
        return iExerciseRecordService.getQuestionSheet(repoId,quType);
    }
}
