package org.dromara.exam.question.service;

import org.dromara.exam.question.domain.vo.QuestionBankVo;
import org.dromara.exam.question.domain.bo.QuestionBankBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 题库Service接口
 *
 * @author LionLi
 * @date 2026-09-28
 */
public interface IQuestionBankService {

    /**
     * 查询题库
     *
     * @param id 主键
     * @return 题库
     */
    QuestionBankVo queryById(Long id);

    /**
     * 分页查询题库列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 题库分页列表
     */
    TableDataInfo<QuestionBankVo> queryPageList(QuestionBankBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的题库列表
     *
     * @param bo 查询条件
     * @return 题库列表
     */
    List<QuestionBankVo> queryList(QuestionBankBo bo);

    /**
     * 新增题库
     *
     * @param bo 题库
     * @return 是否新增成功
     */
    Boolean insertByBo(QuestionBankBo bo);

    /**
     * 修改题库
     *
     * @param bo 题库
     * @return 是否修改成功
     */
    Boolean updateByBo(QuestionBankBo bo);

    /**
     * 校验并批量删除题库信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
