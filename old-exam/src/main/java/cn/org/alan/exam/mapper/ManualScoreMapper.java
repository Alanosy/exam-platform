package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.ManualScore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * 简答题阅卷表 Mapper 接口
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface ManualScoreMapper extends BaseMapper<ManualScore> {

    /**
     * 批量添加批改分数
     *
     * @param manualScores 入参
     * @return 影响记录数
     */
    Integer insertList(List<ManualScore> manualScores);

}
