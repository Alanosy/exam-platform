package org.dromara.exam.manage.service.impl;

import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.core.exception.ServiceException;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.dromara.exam.manage.domain.bo.ExamBo;
import org.dromara.exam.manage.domain.vo.ExamVo;
import org.dromara.exam.manage.domain.Exam;
import org.dromara.exam.manage.mapper.ExamMapper;
import org.dromara.exam.manage.service.IExamService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 考试主Service业务层处理
 *
 * @author LionLi
 * @date 2026-09-29
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ExamServiceImpl implements IExamService {

    /** 公开考试的参加方式 */
    private static final String PARTICIPANT_PUBLIC = "public";

    /** 加入码长度 */
    private static final int JOIN_CODE_LENGTH = 10;

    private final ExamMapper baseMapper;

    /**
     * 查询考试主
     *
     * @param id 主键
     * @return 考试主
     */
    @Override
    public ExamVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询考试主列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 考试主分页列表
     */
    @Override
    public TableDataInfo<ExamVo> queryPageList(ExamBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Exam> lqw = buildQueryWrapper(bo);
        Page<ExamVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的考试主列表
     *
     * @param bo 查询条件
     * @return 考试主列表
     */
    @Override
    public List<ExamVo> queryList(ExamBo bo) {
        LambdaQueryWrapper<Exam> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<Exam> buildQueryWrapper(ExamBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Exam> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(Exam::getId);
        lqw.like(StringUtils.isNotBlank(bo.getExamName()), Exam::getExamName, bo.getExamName());
        // 描述是长文本，按关键词模糊匹配才有意义
        lqw.like(StringUtils.isNotBlank(bo.getExamDesc()), Exam::getExamDesc, bo.getExamDesc());
        lqw.eq(bo.getPaperId() != null, Exam::getPaperId, bo.getPaperId());
        lqw.eq(bo.getStartTime() != null, Exam::getStartTime, bo.getStartTime());
        lqw.eq(bo.getEndTime() != null, Exam::getEndTime, bo.getEndTime());
        lqw.eq(bo.getDuration() != null, Exam::getDuration, bo.getDuration());
        lqw.eq(bo.getAllowLate() != null, Exam::getAllowLate, bo.getAllowLate());
        lqw.eq(bo.getLateMinute() != null, Exam::getLateMinute, bo.getLateMinute());
        lqw.eq(bo.getAllowRetry() != null, Exam::getAllowRetry, bo.getAllowRetry());
        lqw.eq(bo.getMaxRetryCount() != null, Exam::getMaxRetryCount, bo.getMaxRetryCount());
        lqw.eq(StringUtils.isNotBlank(bo.getShowAnswerMode()), Exam::getShowAnswerMode, bo.getShowAnswerMode());
        lqw.eq(StringUtils.isNotBlank(bo.getAntiCheatConfig()), Exam::getAntiCheatConfig, bo.getAntiCheatConfig());
        lqw.eq(StringUtils.isNotBlank(bo.getParticipantType()), Exam::getParticipantType, bo.getParticipantType());
        lqw.eq(StringUtils.isNotBlank(bo.getJoinPassword()), Exam::getJoinPassword, bo.getJoinPassword());
        lqw.eq(bo.getJoinExpireTime() != null, Exam::getJoinExpireTime, bo.getJoinExpireTime());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), Exam::getStatus, bo.getStatus());
        lqw.eq(bo.getCreatorId() != null, Exam::getCreatorId, bo.getCreatorId());
        return lqw;
    }

    /**
     * 新增考试主
     *
     * @param bo 考试主
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(ExamBo bo) {
        Exam add = MapstructUtils.convert(bo, Exam.class);
        // 创建人固定为当前登录用户，不取前端传值（与题库模块保持一致）
        add.setCreatorId(LoginHelper.getUserId());
        if (StringUtils.isBlank(add.getStatus())) {
            add.setStatus("not_start");
        }
        if (ObjectUtil.isNull(add.getDuration())) {
            add.setDuration(0L);
        }
        fillJoinInfo(add, null);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改考试主
     *
     * @param bo 考试主
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(ExamBo bo) {
        Exam update = MapstructUtils.convert(bo, Exam.class);
        validEntityBeforeSave(update);
        // 创建人不允许被修改（与题库模块保持一致）
        update.setCreatorId(null);
        // 编辑时沿用原有的加入码，避免每次保存都换一次链接
        Exam exist = ObjectUtil.isNotNull(bo.getId()) ? baseMapper.selectById(bo.getId()) : null;
        fillJoinInfo(update, ObjectUtil.isNull(exist) ? null : exist.getJoinCode());
        boolean flag = baseMapper.updateById(update) > 0;
        // 全局 updateStrategy=NOT_NULL，实体里置 null 的字段不会进 UPDATE 语句，
        // 所以「公开 → 白名单」要显式清空加入码/密码/有效期
        if (flag && StringUtils.isNotBlank(bo.getParticipantType()) && !PARTICIPANT_PUBLIC.equals(bo.getParticipantType())) {
            LambdaUpdateWrapper<Exam> luw = Wrappers.lambdaUpdate();
            luw.eq(Exam::getId, update.getId());
            luw.set(Exam::getJoinCode, null);
            luw.set(Exam::getJoinPassword, null);
            luw.set(Exam::getJoinExpireTime, null);
            baseMapper.update(null, luw);
        }
        return flag;
    }

    /**
     * 公开考试：没有加入码就生成一个（优先沿用已有的）；非公开考试：清掉加入码/密码/有效期
     *
     * @param exam      待保存的考试
     * @param existCode 库里已有的加入码，为空表示新增或原本没有
     */
    private void fillJoinInfo(Exam exam, String existCode) {
        if (PARTICIPANT_PUBLIC.equals(exam.getParticipantType())) {
            if (StringUtils.isBlank(exam.getJoinCode())) {
                exam.setJoinCode(StringUtils.isNotBlank(existCode) ? existCode : RandomUtil.randomString(JOIN_CODE_LENGTH));
            }
        } else {
            exam.setJoinCode(null);
            exam.setJoinPassword(null);
            exam.setJoinExpireTime(null);
        }
    }

    /**
     * 重新生成公开考试的加入码（原链接立即失效）
     *
     * @param id 考试主键
     * @return 新的加入码
     */
    @Override
    public String refreshJoinCode(Long id) {
        Exam exam = baseMapper.selectById(id);
        if (ObjectUtil.isNull(exam)) {
            throw new ServiceException("考试不存在或已删除");
        }
        if (!StringUtils.equals(exam.getParticipantType(), PARTICIPANT_PUBLIC)) {
            throw new ServiceException("仅公开链接的考试可以生成加入链接");
        }
        String code = RandomUtil.randomString(JOIN_CODE_LENGTH);
        Exam update = new Exam();
        update.setId(id);
        update.setJoinCode(code);
        baseMapper.updateById(update);
        return code;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(Exam entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除考试主信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }
}
