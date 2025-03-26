package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.ExamQuestion;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
 * 试卷试题关联表 Mapper 接口
 *
 * @Author Alan
 * @Version
 * @Date 2024/4/7 3:49 PM
 */
public interface ExamQuestionMapper extends BaseMapper<ExamQuestion> {

    /**
     * 添加试题
     *
     * @param examId              试卷ID
     * @param quType              试题类型
     * @param quScore             试题分数
     * @param questionIdsAndSorts 试题Id与Sort集
     * @return 添加记录数
     */
    int insertQuestion(Integer examId, Integer quType, Integer quScore, List<Map<String, Object>> questionIdsAndSorts);

    /**
     * 根据试卷ID和试题类型查询试卷与试题的联系
     *
     * @param examId 试卷ID
     * @param quType 试题类型
     * @return 试卷试题关联列表
     */
    List<ExamQuestion> getExamQuByExamIdAndQuType(Integer examId, Integer quType);

}
