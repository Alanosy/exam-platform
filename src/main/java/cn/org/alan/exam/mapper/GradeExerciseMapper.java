package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.GradeExercise;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface GradeExerciseMapper extends BaseMapper<GradeExercise> {

    /**
     * 删除用户创建的可供学生练习的题库关联表
     * @param userIds 用户id列表
     * @return 影响记录数
     */
    Integer deleteByUserIds(List<Integer> userIds);
}
