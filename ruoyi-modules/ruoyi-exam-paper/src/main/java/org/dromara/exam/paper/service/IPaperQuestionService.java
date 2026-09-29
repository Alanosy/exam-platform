package org.dromara.exam.paper.service;

import org.dromara.exam.paper.domain.vo.PaperQuestionVo;
import org.dromara.exam.paper.domain.bo.PaperQuestionBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 试卷-试题中间Service接口
 *
 * @author LionLi
 * @date 2026-09-29
 */
public interface IPaperQuestionService {

    /**
     * 查询试卷-试题中间
     *
     * @param id 主键
     * @return 试卷-试题中间
     */
    PaperQuestionVo queryById(Long id);

    /**
     * 分页查询试卷-试题中间列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 试卷-试题中间分页列表
     */
    TableDataInfo<PaperQuestionVo> queryPageList(PaperQuestionBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的试卷-试题中间列表
     *
     * @param bo 查询条件
     * @return 试卷-试题中间列表
     */
    List<PaperQuestionVo> queryList(PaperQuestionBo bo);

    /**
     * 新增试卷-试题中间
     *
     * @param bo 试卷-试题中间
     * @return 是否新增成功
     */
    Boolean insertByBo(PaperQuestionBo bo);

    /**
     * 修改试卷-试题中间
     *
     * @param bo 试卷-试题中间
     * @return 是否修改成功
     */
    Boolean updateByBo(PaperQuestionBo bo);

    /**
     * 校验并批量删除试卷-试题中间信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
