package org.dromara.exam.paper.service;

import org.dromara.exam.paper.domain.vo.PaperVo;
import org.dromara.exam.paper.domain.bo.PaperBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 试卷主Service接口
 *
 * @author LionLi
 * @date 2026-09-29
 */
public interface IPaperService {

    /**
     * 查询试卷主
     *
     * @param id 主键
     * @return 试卷主
     */
    PaperVo queryById(Long id);

    /**
     * 分页查询试卷主列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 试卷主分页列表
     */
    TableDataInfo<PaperVo> queryPageList(PaperBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的试卷主列表
     *
     * @param bo 查询条件
     * @return 试卷主列表
     */
    List<PaperVo> queryList(PaperBo bo);

    /**
     * 新增试卷主
     *
     * @param bo 试卷主
     * @return 是否新增成功
     */
    Boolean insertByBo(PaperBo bo);

    /**
     * 修改试卷主
     *
     * @param bo 试卷主
     * @return 是否修改成功
     */
    Boolean updateByBo(PaperBo bo);

    /**
     * 校验并批量删除试卷主信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
