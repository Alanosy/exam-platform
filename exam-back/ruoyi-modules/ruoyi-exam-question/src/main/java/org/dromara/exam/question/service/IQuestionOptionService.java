package org.dromara.exam.question.service;

import org.dromara.exam.question.domain.vo.QuestionOptionVo;
import org.dromara.exam.question.domain.bo.QuestionOptionBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 试题选项Service接口
 *
 * @author LionLi
 * @date 2026-09-28
 */
public interface IQuestionOptionService {

    /**
     * 查询试题选项
     *
     * @param id 主键
     * @return 试题选项
     */
    QuestionOptionVo queryById(Long id);

    /**
     * 分页查询试题选项列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 试题选项分页列表
     */
    TableDataInfo<QuestionOptionVo> queryPageList(QuestionOptionBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的试题选项列表
     *
     * @param bo 查询条件
     * @return 试题选项列表
     */
    List<QuestionOptionVo> queryList(QuestionOptionBo bo);

    /**
     * 新增试题选项
     *
     * @param bo 试题选项
     * @return 是否新增成功
     */
    Boolean insertByBo(QuestionOptionBo bo);

    /**
     * 修改试题选项
     *
     * @param bo 试题选项
     * @return 是否修改成功
     */
    Boolean updateByBo(QuestionOptionBo bo);

    /**
     * 校验并批量删除试题选项信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
