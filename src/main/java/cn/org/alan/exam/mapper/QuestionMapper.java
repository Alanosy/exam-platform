package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.Question;
import cn.org.alan.exam.model.vo.question.QuestionVO;
import cn.org.alan.exam.model.vo.exercise.QuestionSheetVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * 试题表 Mapper 接口
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 根据试题id获取单题详情
     *
     * @param id 试题id
     * @return 结果集
     */
    QuestionVO selectSingle(Integer id);

    /**
     * 获取试题Id并判断用户是否做过该题
     *
     * @param repoId 题库Id
     * @param quType 试题类型
     * @param userId 用户Id
     * @return 查询结果
     */
    List<QuestionSheetVO> selectQuestionSheet(Integer repoId, Integer quType, Integer userId);

    /**
     * 查询试题详细信息
     *
     * @param id 试题Id
     * @return 结果
     */
    QuestionVO selectDetail(Integer id);

    /**
     * 分页查询试题
     *
     * @param page     分页对象
     * @param userId   用户Id
     * @param roleCode 用户角色代码
     * @param title    试题名称
     * @param type     试题类型
     * @param repoId   题库Id
     * @return 分页结果
     */
    IPage<QuestionVO> selectQuestionPage(IPage<QuestionVO> page, Integer userId, Integer roleCode, String title, Integer type, Integer repoId);

}
