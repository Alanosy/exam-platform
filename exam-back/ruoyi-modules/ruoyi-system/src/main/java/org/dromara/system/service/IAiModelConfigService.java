package org.dromara.system.service;

import org.dromara.system.domain.AiModelConfig;
import org.dromara.system.domain.vo.AiModelConfigVo;
import org.dromara.system.domain.bo.AiModelConfigBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * AI大模型配置Service接口
 *
 * @author Alan
 * @date 2026-10-01
 */
public interface IAiModelConfigService {

    /**
     * 查询AI大模型配置
     *
     * @param id 主键
     * @return AI大模型配置
     */
    AiModelConfigVo queryById(Long id);

    /**
     * 分页查询AI大模型配置列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return AI大模型配置分页列表
     */
    TableDataInfo<AiModelConfigVo> queryPageList(AiModelConfigBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的AI大模型配置列表
     *
     * @param bo 查询条件
     * @return AI大模型配置列表
     */
    List<AiModelConfigVo> queryList(AiModelConfigBo bo);

    /**
     * 新增AI大模型配置
     *
     * @param bo AI大模型配置
     * @return 是否新增成功
     */
    Boolean insertByBo(AiModelConfigBo bo);

    /**
     * 修改AI大模型配置
     *
     * @param bo AI大模型配置
     * @return 是否修改成功
     */
    Boolean updateByBo(AiModelConfigBo bo);

    /**
     * 校验并批量删除AI大模型配置信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
