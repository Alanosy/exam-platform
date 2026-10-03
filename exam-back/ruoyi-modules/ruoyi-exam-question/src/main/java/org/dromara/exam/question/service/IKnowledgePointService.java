package org.dromara.exam.question.service;

import org.dromara.exam.question.domain.bo.KnowledgePointBo;
import org.dromara.exam.question.domain.vo.KnowledgePointVo;
import org.dromara.exam.question.domain.vo.QuestionKnowledgeVo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 知识点Service接口
 *
 * @author ruoyi
 * @date 2026-10-03
 */
public interface IKnowledgePointService {

    /**
     * 查询知识点
     *
     * @param id 主键
     * @return 知识点
     */
    KnowledgePointVo queryById(Long id);

    /**
     * 分页查询知识点列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 知识点分页列表
     */
    TableDataInfo<KnowledgePointVo> queryPageList(KnowledgePointBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的知识点列表（平铺）
     *
     * @param bo 查询条件
     * @return 知识点列表
     */
    List<KnowledgePointVo> queryList(KnowledgePointBo bo);

    /**
     * 查询知识点树（组装好 children，供管理页树表格与各级选择器使用）
     *
     * @param bo 查询条件，传 null 查全量未删除的
     * @return 根节点列表（children 递归）
     */
    List<KnowledgePointVo> queryTreeList(KnowledgePointBo bo);

    /**
     * 新增知识点
     *
     * @param bo 知识点
     * @return 是否新增成功
     */
    Boolean insertByBo(KnowledgePointBo bo);

    /**
     * 修改知识点
     *
     * @param bo 知识点
     * @return 是否修改成功
     */
    Boolean updateByBo(KnowledgePointBo bo);

    /**
     * 校验并批量删除知识点信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 按试题ID批量取知识点（详情回显 / 列表展示用）
     *
     * @param questionIds 试题ID集合
     * @return 试题ID → 知识点列表
     */
    Map<Long, List<KnowledgePointVo>> mapByQuestionIds(Collection<Long> questionIds);

    /**
     * 按试题ID批量取关联明细（含知识点名称）
     *
     * @param questionIds 试题ID集合
     * @return 关联明细列表
     */
    List<QuestionKnowledgeVo> listByQuestionIds(Collection<Long> questionIds);

    /**
     * 保存试题的知识点关联（全量覆盖：先清后插）
     *
     * <p>传 null / 空表示清空关联。
     *
     * @param questionId   试题ID
     * @param knowledgeIds 知识点ID集合
     */
    void saveQuestionKnowledge(Long questionId, List<Long> knowledgeIds);

    /**
     * 清理试题的知识点关联（试题删除时级联调用）
     *
     * @param questionIds 试题ID集合
     */
    void deleteByQuestionIds(Collection<Long> questionIds);
}
