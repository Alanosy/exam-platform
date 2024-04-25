package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.ManualScore;
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
public interface ManualScoreMapper extends BaseMapper<ManualScore> {

    /**
     * 删除用户批改的主观题
     * @param userIds 用户id列表
     * @return 影响记录数
     */
    Integer deleteByUserIds(List<Integer> userIds);
}
