package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.Option;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * 选项表 Mapper 接口
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface OptionMapper extends BaseMapper<Option> {

    /**
     * 批量添加选项
     *
     * @param options 选型列表
     * @return 影响数据库内容的记录数
     */
    Integer insertBatch(List<Option> options);

    /**
     * 根据试题id获取所有选项
     *
     * @param id 试题id
     * @return 结果集
     */
    List<Option> selectAllByQuestionId(Integer id);

    /**
     * 根据单题ID获取所有选项
     *
     * @param id 试题id
     * @return 结果集
     */
    List<Option> selectOptionByqId(Integer id);

    /**
     * 根据试题ID物理删除所有选项（绕过逻辑删除）
     *
     * @param quId 试题id
     * @return 影响行数
     */
    Integer physicalDeleteByQuId(Integer quId);
}
