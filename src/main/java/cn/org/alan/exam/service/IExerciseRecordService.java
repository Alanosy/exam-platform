package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.ExerciseRecord;
import cn.org.alan.exam.model.form.exercise.ExerciseFillAnswerFrom;
import cn.org.alan.exam.model.vo.question.QuestionVO;
import cn.org.alan.exam.model.vo.exercise.AnswerInfoVO;
import cn.org.alan.exam.model.vo.exercise.QuestionSheetVO;
import cn.org.alan.exam.model.vo.record.ExamRecordDetailVO;
import cn.org.alan.exam.model.vo.record.ExamRecordVO;
import cn.org.alan.exam.model.vo.record.ExerciseRecordDetailVO;
import cn.org.alan.exam.model.vo.record.ExerciseRecordVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 考试记录服务接口
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface IExerciseRecordService extends IService<ExerciseRecord> {


    /**
     * 获取试题答题卡列表
     *
     * @param repoId 题库Id
     * @param quType 试题类型
     * @return 响应结果
     */
    Result<List<QuestionSheetVO>> getQuestionSheet(Integer repoId, Integer quType);

    /**
     * 分页查询已考试试卷
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param examName 考试名称
     * @param isASC    是否升序排列，true为升序，false为降序
     * @return 查询结果
     */
    Result<IPage<ExamRecordVO>> getExamRecordPage(Integer pageNum, Integer pageSize, String examName, Boolean isASC);

    /**
     * 查询某场考试的信息
     *
     * @param examId 试卷ID
     * @return
     */
    Result<List<ExamRecordDetailVO>> getExamRecordDetail(Integer examId);

    /**
     * 查询所考过的刷题
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return
     */
    Result<IPage<ExerciseRecordVO>> getExerciseRecordPage(Integer pageNum, Integer pageSize, String repoName);

    /**
     * 查询刷题详情
     *
     * @param exerciseId 刷题记录ID
     * @return
     */
    Result<List<ExerciseRecordDetailVO>> getExerciseRecordDetail(Integer exerciseId);

    /**
     * 填充答案，并返回试题信息
     *
     * @param exerciseFillAnswerFrom 请求参数
     * @return 响应结果
     */
    Result<QuestionVO> fillAnswer(ExerciseFillAnswerFrom exerciseFillAnswerFrom);

    /**
     * 获取单题，没有选项
     *
     * @param id 试题ID
     * @return
     */
    Result<QuestionVO> getSingle(Integer id);

    /**
     * 获取用户回答详情
     *
     * @param repoId 题库ID
     * @param quId   试题ID
     * @return
     */
    Result<AnswerInfoVO> getAnswerInfo(Integer repoId, Integer quId);
}
