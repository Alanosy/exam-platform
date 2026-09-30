package org.dromara.exam.manage.service;

import org.dromara.exam.manage.domain.vo.ExamInviteVo;
import org.dromara.exam.manage.domain.bo.ExamInviteBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 考试邀请记录Service接口
 *
 * @author LionLi
 * @date 2026-09-30
 */
public interface IExamInviteService {

    /**
     * 查询考试邀请记录
     *
     * @param id 主键
     * @return 考试邀请记录
     */
    ExamInviteVo queryById(Long id);

    /**
     * 分页查询考试邀请记录列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 考试邀请记录分页列表
     */
    TableDataInfo<ExamInviteVo> queryPageList(ExamInviteBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的考试邀请记录列表
     *
     * @param bo 查询条件
     * @return 考试邀请记录列表
     */
    List<ExamInviteVo> queryList(ExamInviteBo bo);

    /**
     * 新增考试邀请记录
     *
     * @param bo 考试邀请记录
     * @return 是否新增成功
     */
    Boolean insertByBo(ExamInviteBo bo);

    /**
     * 修改考试邀请记录
     *
     * @param bo 考试邀请记录
     * @return 是否修改成功
     */
    Boolean updateByBo(ExamInviteBo bo);

    /**
     * 校验并批量删除考试邀请记录信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
