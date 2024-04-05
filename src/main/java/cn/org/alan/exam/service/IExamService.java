package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.Exam;
import cn.org.alan.exam.model.form.ExamForm;
import cn.org.alan.exam.model.vo.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface IExamService extends IService<Exam> {
    /**
     * 创建考试
     * @param examForm
     * @return
     */
    Result<String> createExam(ExamForm examForm);

    /**
     * 修改考试
     * @param examForm
     * @return
     */
    Result<String> updateExam(ExamForm examForm);

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
    Result<IPage<ExamVO>> getPagingExam(Integer pageNum, Integer pageSize, Integer title);

    /**
     * 获取考试题目id列表
     * @param id
     * @return
     */
    Result<ExamDetailRespVO> getQuestionList(String id);

    /**
     * 获取单题信息
     * @param examId
     * @param questionId
     * @return
     */
    Result<PaperQuDetailVO> getQuestionSingle(String examId, String questionId);

    /**
     * 题目汇总
     * @param id
     * @return
     */
    Result<EXamQuCollectVO> getCollect(String id);

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
    Result<String> addCheat();

    /**
     * 填充答案
     * @return
     */
    Result<ExamFillVO> addAnswer();

    /**
     * 根据班级获得考试
     * @param id
     * @return
     */
    Result<ExamVO> getGradeExamList(String id);
}
