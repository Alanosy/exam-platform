package org.dromara.exam.question.service;

import org.dromara.exam.question.domain.vo.QuestionMediaVo;
import org.dromara.exam.question.domain.bo.QuestionMediaBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 试题多媒体附件Service接口
 *
 * @author LionLi
 * @date 2026-09-28
 */
public interface IQuestionMediaService {

    /**
     * 查询试题多媒体附件
     *
     * @param id 主键
     * @return 试题多媒体附件
     */
    QuestionMediaVo queryById(Long id);

    /**
     * 分页查询试题多媒体附件列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 试题多媒体附件分页列表
     */
    TableDataInfo<QuestionMediaVo> queryPageList(QuestionMediaBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的试题多媒体附件列表
     *
     * @param bo 查询条件
     * @return 试题多媒体附件列表
     */
    List<QuestionMediaVo> queryList(QuestionMediaBo bo);

    /**
     * 新增试题多媒体附件
     *
     * @param bo 试题多媒体附件
     * @return 是否新增成功
     */
    Boolean insertByBo(QuestionMediaBo bo);

    /**
     * 修改试题多媒体附件
     *
     * @param bo 试题多媒体附件
     * @return 是否修改成功
     */
    Boolean updateByBo(QuestionMediaBo bo);

    /**
     * 校验并批量删除试题多媒体附件信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
