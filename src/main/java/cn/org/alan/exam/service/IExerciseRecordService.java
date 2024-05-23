package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.ExerciseRecord;
import cn.org.alan.exam.model.form.ExerciseFillAnswerFrom;
import cn.org.alan.exam.model.vo.QuestionVO;
import cn.org.alan.exam.model.vo.exercise.QuestionSheetVO;
import cn.org.alan.exam.model.vo.record.ExamRecordDetailVO;
import cn.org.alan.exam.model.vo.record.ExamRecordVO;
import cn.org.alan.exam.model.vo.record.ExerciseRecordDetailVO;
import cn.org.alan.exam.model.vo.record.ExerciseRecordVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
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

    /**
     * 查询所考过的考试
     * @param pageNum
     * @param pageSize
     * @return
     */
    Result<IPage<ExamRecordVO>> getExamRecordPage(Integer pageNum, Integer pageSize);

    /**
     * 查询某场考试的信息
     * @param examId
     * @return
     */
    Result<List<ExamRecordDetailVO>> getExamRecordDetail(Integer examId);

    /**
     * 查询所考过的刷题
     * @param pageNum
     * @param pageSize
     * @return
     */
    Result<IPage<ExerciseRecordVO>> getExerciseRecordPage(Integer pageNum, Integer pageSize);

    /**
     * 查询刷题详情
     * @param examId
     * @return
     */
    Result<List<ExerciseRecordDetailVO>> getExerciseRecordDetail(Integer exerciseId);

    /**
     * 填充答案，并返回试题信息
     * @param exerciseFillAnswerFrom 请求参数
     * @return 响应结果
     */
    Result<QuestionVO> fillAnswer(ExerciseFillAnswerFrom exerciseFillAnswerFrom);

    /**
     * 获取单题，没有选项
     * @param id
     * @return
     */
    Result<QuestionVO> getSingle(Integer id);
}
