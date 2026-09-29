package org.dromara.exam.question.service;

import org.dromara.exam.question.domain.bo.QuestionMediaBo;
import org.dromara.exam.question.domain.bo.QuestionMediaSaveBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.exam.question.domain.vo.QuestionMediaVo;

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

    /**
     * 登记一笔尚未挂到试题上的上传记录
     *
     * <p>富文本里插入图片时对象已经进了存储桶，但此时试题可能还没保存。这里先落一条
     * question_id 为空的记录，待试题保存时再回填 question_id；始终没回填的会被
     * {@link #cleanUnused(int)} 清理掉，避免弃稿图片永久占用存储。
     *
     * @param bo 媒体附件信息
     * @return 新增记录的主键
     */
    Long insertDraft(QuestionMediaSaveBo bo);

    /**
     * 清理长时间未挂到任何试题上的媒体附件（含对象存储里的文件）
     *
     * @param retainHours 保留时长（小时），超过该时长仍未归属试题的记录会被清理
     * @return 清理的记录数量
     */
    Integer cleanUnused(int retainHours);
}
