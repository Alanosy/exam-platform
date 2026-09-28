package org.dromara.exam.question.service;

import org.dromara.exam.question.domain.vo.QuestionTagRelVo;
import org.dromara.exam.question.domain.bo.QuestionTagRelBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 试题标签关联Service接口
 *
 * @author LionLi
 * @date 2026-09-28
 */
public interface IQuestionTagRelService {

    /**
     * 查询试题标签关联
     *
     * @param id 主键
     * @return 试题标签关联
     */
    QuestionTagRelVo queryById(Long id);

    /**
     * 分页查询试题标签关联列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 试题标签关联分页列表
     */
    TableDataInfo<QuestionTagRelVo> queryPageList(QuestionTagRelBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的试题标签关联列表
     *
     * @param bo 查询条件
     * @return 试题标签关联列表
     */
    List<QuestionTagRelVo> queryList(QuestionTagRelBo bo);

    /**
     * 新增试题标签关联
     *
     * @param bo 试题标签关联
     * @return 是否新增成功
     */
    Boolean insertByBo(QuestionTagRelBo bo);

    /**
     * 修改试题标签关联
     *
     * @param bo 试题标签关联
     * @return 是否修改成功
     */
    Boolean updateByBo(QuestionTagRelBo bo);

    /**
     * 校验并批量删除试题标签关联信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
