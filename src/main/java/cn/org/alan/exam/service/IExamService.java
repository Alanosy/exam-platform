package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.Exam;
import cn.org.alan.exam.model.form.exam.ExamAddForm;
import cn.org.alan.exam.model.form.exam.ExamUpdateForm;
import cn.org.alan.exam.model.vo.exam.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 *  服务类
 *
 * @author Alan
 * @since 2024-03-21
 */
public interface IExamService extends IService<Exam> {
    /**
     * 创建考试
     * @param examAddForm
     * @return
     */
    Result<String> createExam(ExamAddForm examAddForm);

    /**
     * 修改考试
     * @param examUpdateForm
     * @return
     */
    Result<String> updateExam(ExamUpdateForm examUpdateForm, Integer id);

    /**
     * 删除考试
     * @param ids
     * @return
     */
    Result<String> deleteExam(String ids);

    /**
     * 教师分页查找考试列表
     * @param pageNum
     * @param pageSize
     * @param title
     * @return
     */
    Result<IPage<ExamVO>> getPagingExam(Integer pageNum, Integer pageSize, String title);

    /**
     * 获取考试题目id列表
     * @param id
     * @return
     */
    Result<List<ExamDetailRespVO>> getQuestionList(String id);

    /**
     * 获取单题信息
     * @param examId
     * @param questionId
     * @return
     */
    Result<ExamQuDetailVO> getQuestionSingle(Integer examId, Integer questionId);

    /**
     * 题目汇总
     * @param examId
     * @return
     */
    Result<List<ExamQuCollectVO>> getCollect(Integer examId);

    /**
     * 获取考试详情信息
     * @param id
     * @return
     */
    Result<ExamDetailVO> getDetail(String id);

    /**
     * 考试作弊次数添加
     * @return
     */
    Result<String> addCheat(Integer examId);

    /**
     * 填充答案
     * @return
     */
    Result<String> addAnswer(ExamQuAnswerVO examQuAnswerForm);

    /**
     * 根据班级获得考试
     *
     * @param gradeId
     * @return
     */
    Result<IPage<ExamVO>> getGradeExamList(Integer gradeId, Integer pageNum, Integer pageSize);

    /**
     * 交卷操作
     * @param examId
     * @return
     */
    Result<ExamQuDetailVO> handExam(Integer examId);

    /**
     * 开始考试
     * @return
     */
    Result<String> startExam(Integer examId);



}
