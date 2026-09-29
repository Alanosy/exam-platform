package org.dromara.exam.question.service;

import org.dromara.exam.question.domain.vo.QuestionBankCategoryVo;
import org.dromara.exam.question.domain.bo.QuestionBankCategoryBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 题库分类目录Service接口
 *
 * @author LionLi
 * @date 2026-09-29
 */
public interface IQuestionBankCategoryService {

    /**
     * 查询题库分类目录
     *
     * @param id 主键
     * @return 题库分类目录
     */
    QuestionBankCategoryVo queryById(Long id);

    /**
     * 分页查询题库分类目录列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 题库分类目录分页列表
     */
    TableDataInfo<QuestionBankCategoryVo> queryPageList(QuestionBankCategoryBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的题库分类目录列表
     *
     * @param bo 查询条件
     * @return 题库分类目录列表
     */
    List<QuestionBankCategoryVo> queryList(QuestionBankCategoryBo bo);

    /**
     * 查询分类树（全量，供前端树表格与树选择使用）
     *
     * @param bo 查询条件
     * @return 分类列表（平铺，前端自行构树）
     */
    List<QuestionBankCategoryVo> queryTreeList(QuestionBankCategoryBo bo);

    /**
     * 新增题库分类目录
     *
     * @param bo 题库分类目录
     * @return 是否新增成功
     */
    Boolean insertByBo(QuestionBankCategoryBo bo);

    /**
     * 修改题库分类目录
     *
     * @param bo 题库分类目录
     * @return 是否修改成功
     */
    Boolean updateByBo(QuestionBankCategoryBo bo);

    /**
     * 校验并批量删除题库分类目录信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
