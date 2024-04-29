package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.ExerciseRecord;
import cn.org.alan.exam.model.vo.exercise.QuestionSheetVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface IExerciseRecordService extends IService<ExerciseRecord> {


    /**
     * 获取试题答题卡列表
     * @param repoId 题库Id
     * @param quType 试题类型
     * @return 响应结果
     */
    Result<List<QuestionSheetVO>> getQuestionSheet(Integer repoId, Integer quType);
}
