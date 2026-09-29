package org.dromara.exam.manage.service;

import org.dromara.exam.manage.domain.vo.ExamVo;
import org.dromara.exam.manage.domain.bo.ExamBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 考试主Service接口
 *
 * @author LionLi
 * @date 2026-09-29
 */
public interface IExamService {

    /**
     * 查询考试主
     *
     * @param id 主键
     * @return 考试主
     */
    ExamVo queryById(Long id);

    /**
     * 分页查询考试主列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 考试主分页列表
     */
    TableDataInfo<ExamVo> queryPageList(ExamBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的考试主列表
     *
     * @param bo 查询条件
     * @return 考试主列表
     */
    List<ExamVo> queryList(ExamBo bo);

    /**
     * 新增考试主
     *
     * @param bo 考试主
     * @return 是否新增成功
     */
    Boolean insertByBo(ExamBo bo);

    /**
     * 修改考试主
     *
     * @param bo 考试主
     * @return 是否修改成功
     */
    Boolean updateByBo(ExamBo bo);

    /**
     * 重新生成公开考试的加入码，原加入链接立即失效
     *
     * @param id 考试主键
     * @return 新的加入码
     */
    String refreshJoinCode(Long id);

    /**
     * 校验并批量删除考试主信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
