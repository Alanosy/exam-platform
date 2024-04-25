package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.Grade;
import cn.org.alan.exam.model.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Repository
public interface GradeMapper extends BaseMapper<Grade> {

    Integer getGradeCount(Integer exam_id);
    /**
     * 删除用户创建的班级
     *
     * @param userIds 用户id列表
     * @return 影响记录数
     */
    Integer deleteByUserId(List<Integer> userIds);

    /**
     * 根据用户id获取班级列表
     *
     * @param userIds 用户id列表
     * @return 查询结果集
     */
    List<Integer> selectIdsByUserIds(List<Integer> userIds);
}
