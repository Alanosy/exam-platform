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
import org.dromara.exam.manage.domain.vo.ExamJoinVo;
import org.dromara.exam.manage.domain.Exam;
import org.dromara.exam.manage.domain.ExamInvite;
import org.dromara.exam.manage.mapper.ExamMapper;
import org.dromara.exam.manage.mapper.ExamInviteMapper;
import org.dromara.exam.manage.service.IExamService;

import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Date;

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

    /** 邀请记录里「通过链接加入」的类型 */
    private static final String INVITE_TYPE_LINK = "link";

    /** 邀请记录里「已进入考试」的状态 */
    private static final String INVITE_STATUS_ACCEPT = "accept";

    private final ExamMapper baseMapper;

    private final ExamInviteMapper examInviteMapper;

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
        lqw.eq(StringUtils.isNotBlank(bo.getExamType()), Exam::getExamType, bo.getExamType());
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
        // 兜底为正式考试：老数据与没选类型的场景都按最严的那套规则走
        if (StringUtils.isBlank(add.getExamType())) {
            add.setExamType(Exam.TYPE_FORMAL);
        }
        // 练习考试次数不限、随手重练，管理端默认把「允许多次作答」打开
        if (Exam.TYPE_PRACTICE.equals(add.getExamType()) && ObjectUtil.isNull(add.getAllowRetry())) {
            add.setAllowRetry(1L);
        }
        fillJoinInfo(add, null);
        validEntityBeforeSave(add);
        checkExamRules(add, null);
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
        Exam exist = ObjectUtil.isNotNull(bo.getId()) ? baseMapper.selectById(bo.getId()) : null;
        if (ObjectUtil.isNull(exist)) {
            throw new ServiceException("考试不存在或已删除");
        }
        checkExamRules(update, exist);
        // 创建人不允许被修改（与题库模块保持一致）
        update.setCreatorId(null);
        // 编辑时沿用原有的加入码，避免每次保存都换一次链接
        fillJoinInfo(update, exist.getJoinCode());
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
     * 按加入码查询公开考试的加入信息
     *
     * @param joinCode 加入码
     * @return 加入信息
     */
    @Override
    public ExamJoinVo queryJoinInfo(String joinCode) {
        Exam exam = selectByJoinCode(joinCode);
        String account = LoginHelper.getUsername();

        ExamJoinVo vo = new ExamJoinVo();
        vo.setExamId(exam.getId());
        vo.setExamName(exam.getExamName());
        vo.setExamDesc(exam.getExamDesc());
        vo.setStartTime(exam.getStartTime());
        vo.setEndTime(exam.getEndTime());
        vo.setDuration(exam.getDuration());
        vo.setStatus(exam.getStatus());
        vo.setJoinExpireTime(exam.getJoinExpireTime());
        // 只告诉前端要不要输密码，密码本身不下发
        vo.setNeedPassword(StringUtils.isNotBlank(exam.getJoinPassword()));
        vo.setJoined(joined(exam.getId(), account));
        String tip = joinTip(exam);
        vo.setJoinable(tip == null);
        vo.setJoinTip(tip);
        return vo;
    }

    /**
     * 通过加入码加入公开考试
     *
     * @param joinCode 加入码
     * @param password 参与密码
     * @return 考试ID
     */
    @Override
    public Long joinByCode(String joinCode, String password) {
        Exam exam = selectByJoinCode(joinCode);
        String tip = joinTip(exam);
        if (StringUtils.isNotBlank(tip)) {
            throw new ServiceException(tip);
        }
        if (StringUtils.isNotBlank(exam.getJoinPassword()) && !exam.getJoinPassword().equals(password)) {
            throw new ServiceException("参与密码错误");
        }
        String account = LoginHelper.getUsername();
        if (StringUtils.isBlank(account)) {
            throw new ServiceException("登录状态已失效，请重新登录");
        }
        // 重复点「加入」不重复记记录
        if (joined(exam.getId(), account)) {
            return exam.getId();
        }
        ExamInvite invite = new ExamInvite();
        invite.setExamId(exam.getId());
        invite.setInviteAccount(account);
        invite.setInviteType(INVITE_TYPE_LINK);
        invite.setInviteStatus(INVITE_STATUS_ACCEPT);
        invite.setInviteTime(new Date());
        examInviteMapper.insert(invite);
        return exam.getId();
    }

    /**
     * 按加入码取考试，顺带校验链接本身是否可用
     */
    private Exam selectByJoinCode(String joinCode) {
        if (StringUtils.isBlank(joinCode)) {
            throw new ServiceException("加入码不能为空");
        }
        LambdaQueryWrapper<Exam> lqw = Wrappers.lambdaQuery();
        lqw.eq(Exam::getJoinCode, joinCode).last("limit 1");
        Exam exam = baseMapper.selectOne(lqw);
        if (ObjectUtil.isNull(exam)) {
            throw new ServiceException("加入链接无效，请向考试组织者确认");
        }
        if (!PARTICIPANT_PUBLIC.equals(exam.getParticipantType())) {
            throw new ServiceException("该考试未开放公开链接加入");
        }
        return exam;
    }

    /**
     * 判断当前能否加入，可以加入时返回 null，否则返回不可加入的原因
     */
    private String joinTip(Exam exam) {
        if ("archived".equals(exam.getStatus())) {
            return "该考试已归档，无法加入";
        }
        if ("finished".equals(exam.getStatus())) {
            return "该考试已结束，无法加入";
        }
        Date now = new Date();
        // 链接有效期优先于考试结束时间，没配就与考试结束时间一致
        Date expireTime = ObjectUtil.isNotNull(exam.getJoinExpireTime()) ? exam.getJoinExpireTime() : exam.getEndTime();
        if (ObjectUtil.isNotNull(expireTime) && expireTime.before(now)) {
            return ObjectUtil.isNotNull(exam.getJoinExpireTime()) ? "加入链接已过期" : "该考试已结束，无法加入";
        }
        return null;
    }

    /**
     * 当前账号是否已经通过链接加入过这场考试
     */
    private boolean joined(Long examId, String account) {
        if (StringUtils.isBlank(account)) {
            return false;
        }
        LambdaQueryWrapper<ExamInvite> lqw = Wrappers.lambdaQuery();
        lqw.eq(ExamInvite::getExamId, examId).eq(ExamInvite::getInviteAccount, account).eq(ExamInvite::getInviteType, INVITE_TYPE_LINK).last("limit 1");
        return ObjectUtil.isNotNull(examInviteMapper.selectOne(lqw));
    }

    /**
     * 按考试类型校验活动规则
     *
     * <p>正式考试必须有起止时间（考试窗口的概念）；练习考试是长期可练的，
     * 时间可以留空，只有填了才校验先后顺序。
     *
     * @param exam  本次要写的字段（update 时未改动的字段为 null）
     * @param exist 库里已有的记录，新增传 null；用于补全 update 时没带的字段，避免误判
     */
    private void checkExamRules(Exam exam, Exam exist) {
        String type = StringUtils.isNotBlank(exam.getExamType()) ? exam.getExamType()
            : (ObjectUtil.isNull(exist) ? null : exist.getExamType());
        Date startTime = ObjectUtil.isNotNull(exam.getStartTime()) ? exam.getStartTime()
            : (ObjectUtil.isNull(exist) ? null : exist.getStartTime());
        Date endTime = ObjectUtil.isNotNull(exam.getEndTime()) ? exam.getEndTime()
            : (ObjectUtil.isNull(exist) ? null : exist.getEndTime());

        boolean practice = Exam.TYPE_PRACTICE.equals(type);
        if (!practice) {
            if (ObjectUtil.isNull(startTime)) {
                throw new ServiceException("正式考试必须填写开始时间");
            }
            if (ObjectUtil.isNull(endTime)) {
                throw new ServiceException("正式考试必须填写结束时间");
            }
        }
        if (ObjectUtil.isNotNull(startTime) && ObjectUtil.isNotNull(endTime) && !endTime.after(startTime)) {
            throw new ServiceException("结束时间必须晚于开始时间");
        }
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
