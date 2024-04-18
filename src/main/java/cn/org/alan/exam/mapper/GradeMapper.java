package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.Grade;
import cn.org.alan.exam.model.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Repository
public interface GradeMapper extends BaseMapper<Grade> {

    Map<Integer, Integer> selectMaps(QueryWrapper<User> queryWrapper, String gradeId, String s);
}
