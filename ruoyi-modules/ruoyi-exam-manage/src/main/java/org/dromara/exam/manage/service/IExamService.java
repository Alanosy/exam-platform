package org.dromara.exam.manage.service;

import org.dromara.exam.manage.domain.vo.ExamVo;
import org.dromara.exam.manage.domain.vo.ExamJoinVo;
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
     * 按加入码查询公开考试的加入信息
     *
     * <p>考生打开加入链接时展示的考试概要，不含参与密码本身；
     * 同时回传链接当前是否可加入，以及当前登录用户是否已经加入过。
     *
     * @param joinCode 加入码
     * @return 加入信息
     */
    ExamJoinVo queryJoinInfo(String joinCode);

    /**
     * 通过加入码加入公开考试
     *
     * <p>校验链接有效性、参与密码、有效期与考试状态，通过后写一条邀请记录，
     * 供考试中心查询「我参与的考试」。同一账号重复加入只记一条。
     *
     * @param joinCode 加入码
     * @param password 参与密码，考试未设置密码时传空
     * @return 考试ID
     */
    Long joinByCode(String joinCode, String password);

    /**
     * 校验并批量删除考试主信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
