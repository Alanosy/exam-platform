package cn.org.alan.exam.service;

import cn.org.alan.exam.model.entity.ExamQuAnswer;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 自动评分接口
 *
 * @author 赵浩森
 * @since 2025/4/12 18:57
 * @version 1.0
 */
public interface IAutoScoringService extends IService<ExamQuAnswer> {
    /**
     * 自动评分服务
     * @param examId 考试ID
     * @param userId 用户ID
     * @return 评分结果列表
     */
    void autoScoringExam(Integer examId, Integer userId);
} 