package org.dromara.exam.practice.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.exam.practice.domain.WrongQuestion;
import org.dromara.exam.practice.domain.vo.WrongQuestionVo;

/**
 * 用户错题集Mapper接口
 *
 * @author ruoyi
 * @date 2026-09-30
 */
public interface WrongQuestionMapper extends BaseMapperPlus<WrongQuestion, WrongQuestionVo> {

    /**
     * 查同一用户同一道题的记录，**包含已软删除的**
     *
     * <p>BaseMapper 的查询都会被 @TableLogic 追加 is_deleted = 0，
     * 已删除的行查不到，同步错题时会走到新增分支，撞上 uk_user_question 唯一键。
     * 手写 SQL 不受逻辑删除影响，租户条件仍由租户拦截器自动追加。
     *
     * @param userId     用户ID
     * @param questionId 题目ID
     * @return 已有记录，没有则 null
     */
    @Select("select * from wrong_question where user_id = #{userId} and question_id = #{questionId} limit 1")
    WrongQuestion selectOneIgnoreDeleted(@Param("userId") Long userId, @Param("questionId") Long questionId);

    /**
     * 复活已软删除的错题
     *
     * <p>updateById 生成的 SQL 会带 is_deleted = 0，删过的行根本更新不到，
     * 所以要先手工把删除标记清掉。
     *
     * @param id 错题ID
     * @return 影响行数
     */
    @Update("update wrong_question set is_deleted = 0 where id = #{id}")
    int restore(@Param("id") Long id);
}
