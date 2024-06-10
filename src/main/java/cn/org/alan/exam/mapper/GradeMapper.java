package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.Grade;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.vo.GradeVO;
import cn.org.alan.exam.model.vo.stat.GradeExamVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    List<Integer> selectIdsByUserIds(List<Integer> userIds);

    Integer deleteByUserId(List<Integer> userIds);

    Page<GradeVO> selectGradePage(Page<GradeVO> page, Integer userId , String gradeName, Integer role);

    List<Integer> selectGradeIdsPage(Integer userId, String gradeName, int offset, Integer pageSize);

    List<GradeVO> batchSelectByIds(List<Integer> missIds);

    int countByCondition(Integer userId, String gradeName);
}
