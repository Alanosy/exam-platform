package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.Exam;
import cn.org.alan.exam.model.entity.ExamQuAnswer;
import cn.org.alan.exam.model.form.exam.ExamAddForm;
import cn.org.alan.exam.model.form.exam.ExamQuAnswerFrom;
import cn.org.alan.exam.model.form.exam.ExamUpdateForm;
import cn.org.alan.exam.model.form.examquanswer.ExamQuAnswerAddForm;
import cn.org.alan.exam.model.vo.exam.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 考试服务类
 *
 * @author Alan
 * @since 2024-03-21
 */
public interface IExamService extends IService<Exam> {
    /**
     * 创建考试
     *
     * @param examAddForm
     * @return
     */
    Result<String> createExam(ExamAddForm examAddForm);

    /**
     * 修改考试
     *
     * @param examUpdateForm
     * @return
     */
    Result<String> updateExam(ExamUpdateForm examUpdateForm, Integer examId);

    /**
     * 删除考试
     *
     * @param ids
     * @return
     */
    Result<String> deleteExam(String ids);

    /**
     * 教师分页查找考试列表
     *
     * @param pageNum
     * @param pageSize
     * @param title
     * @return
     */
    Result<IPage<ExamVO>> getPagingExam(Integer pageNum, Integer pageSize, String title);

    /**
     * 获取考试题目id列表
     *
     * @param examId
     * @return
     */
    Result<ExamQuestionListVO> getQuestionList(Integer examId);

    /**
     * 获取单题信息
     *
     * @param examId
     * @param questionId
     * @return
     */
    Result<ExamQuDetailVO> getQuestionSingle(Integer examId, Integer questionId);

    /**
     * 题目汇总
     *
     * @param examId
     * @return
     */
    Result<List<ExamQuCollectVO>> getCollect(Integer examId);

    /**
     * 获取考试详情信息
     *
     * @param examId
     * @return
     */
    Result<ExamDetailVO> getDetail(Integer examId);

    /**
     * 考试作弊次数添加
     *
     * @return
     */
    Result<String> addCheat(Integer examId);

    /**
     * 填充答案
     *
     * @return
     */
    Result<String> addAnswer(ExamQuAnswerAddForm examQuAnswerForm);

    /**
     * 根据班级获得考试
     *
     * @return
     */
    Result<List<ExamGradeListVO>> getGradeExamList(Integer pageNum, Integer pageSize,String title);

    /**
     * 交卷操作
     *
     * @param examId
     * @return
     */
    Result<ExamQuDetailVO> handExam(Integer examId);

    /**
     * 开始考试
     *
     * @return
     */
    Result<String> startExam(Integer examId);

    /**
     * 添加考试记录
     *
     * @param examQuAnswerForm
     * @param quType
     * @return
     */
    Result<String> insertNewAnswer(ExamQuAnswerAddForm examQuAnswerForm, Integer quType);

    /**
     * 修改考试记录
     *
     * @param examQuAnswerForm
     * @param quType
     * @return
     */
    Result<String> updateAnswerIfExists(ExamQuAnswerAddForm examQuAnswerForm, Integer quType);

    /**
     * 表单转化实体
     *
     * @param form
     * @param quType
     * @return
     */
    ExamQuAnswer prepareExamQuAnswer(ExamQuAnswerAddForm form, Integer quType);

    /**
     * 判断用户是否正在考试
     *
     * @param examId
     * @return
     */
    boolean isUserTakingExam(Integer examId);
}
