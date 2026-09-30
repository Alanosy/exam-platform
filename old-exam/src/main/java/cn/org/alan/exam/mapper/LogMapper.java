package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.Log;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 说明：
 * 日志表Mapper
 *
 * @Author Alan
 * @Version 1.0
 * @Date 2025/4/4 2:55 PM
 */
@Mapper
public interface LogMapper extends BaseMapper<Log> {
}
