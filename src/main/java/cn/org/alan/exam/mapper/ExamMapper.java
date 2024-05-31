package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.Exam;
import cn.org.alan.exam.model.vo.answer.AnswerExamVO;
import cn.org.alan.exam.model.vo.record.ExamRecordVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Mapper 接口
 *
 * @author Alan
 * @since 2024-03-21
 */

public interface ExamMapper extends BaseMapper<Exam> {

    int delExam(List<Integer> examIds);

    int delExamGrade(List<Integer> examIds);

    int delExamRepo(List<Integer> examIds);

    int delExamQue(List<Integer> examIds);

    /**
     * 删除用户创建的考试
     *
     * @param userIds 用户id列表
     * @return 影响数据库记录数
     */
    Integer deleteByUserIds(List<Integer> userIds);


    /**
     * 获取自己创建的考试，考试id，考试考试标题，是否需要阅卷
     *
     * @param userId
     * @return
     */
    IPage<AnswerExamVO> selectMarkedList(@Param("page") IPage<AnswerExamVO> page, @Param("userId") Integer userId,String role);

    Page<ExamRecordVO> getExamRecordPage(Page<ExamRecordVO> examPage, Integer userId);
}
