package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.Category;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分类Mapper接口
 *
 * @author Moxuec
 * @since 2025-04-09
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}