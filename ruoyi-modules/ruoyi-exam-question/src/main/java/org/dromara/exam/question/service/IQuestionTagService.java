package org.dromara.exam.question.service;

import org.dromara.exam.question.domain.vo.QuestionTagVo;
import org.dromara.exam.question.domain.bo.QuestionTagBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 试题标签Service接口
 *
 * @author LionLi
 * @date 2026-09-28
 */
public interface IQuestionTagService {

    /**
     * 查询试题标签
     *
     * @param id 主键
     * @return 试题标签
     */
    QuestionTagVo queryById(Long id);

    /**
     * 分页查询试题标签列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 试题标签分页列表
     */
    TableDataInfo<QuestionTagVo> queryPageList(QuestionTagBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的试题标签列表
     *
     * @param bo 查询条件
     * @return 试题标签列表
     */
    List<QuestionTagVo> queryList(QuestionTagBo bo);

    /**
     * 新增试题标签
     *
     * @param bo 试题标签
     * @return 是否新增成功
     */
    Boolean insertByBo(QuestionTagBo bo);

    /**
     * 修改试题标签
     *
     * @param bo 试题标签
     * @return 是否修改成功
     */
    Boolean updateByBo(QuestionTagBo bo);

    /**
     * 校验并批量删除试题标签信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
