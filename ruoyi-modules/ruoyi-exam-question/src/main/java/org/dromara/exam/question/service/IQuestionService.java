package org.dromara.exam.question.service;

import org.dromara.exam.question.domain.vo.QuestionVo;
import org.dromara.exam.question.domain.bo.QuestionBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 试题主Service接口
 *
 * @author LionLi
 * @date 2026-09-28
 */
public interface IQuestionService {

    /**
     * 查询试题主
     *
     * @param id 主键
     * @return 试题主
     */
    QuestionVo queryById(Long id);

    /**
     * 分页查询试题主列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 试题主分页列表
     */
    TableDataInfo<QuestionVo> queryPageList(QuestionBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的试题主列表
     *
     * @param bo 查询条件
     * @return 试题主列表
     */
    List<QuestionVo> queryList(QuestionBo bo);

    /**
     * 新增试题主
     *
     * @param bo 试题主
     * @return 是否新增成功
     */
    Boolean insertByBo(QuestionBo bo);

    /**
     * 修改试题主
     *
     * @param bo 试题主
     * @return 是否修改成功
     */
    Boolean updateByBo(QuestionBo bo);

    /**
     * 校验并批量删除试题主信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
