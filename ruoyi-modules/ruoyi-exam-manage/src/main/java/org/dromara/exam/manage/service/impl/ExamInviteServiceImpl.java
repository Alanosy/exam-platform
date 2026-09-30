package org.dromara.exam.manage.service.impl;

import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.dromara.exam.manage.domain.bo.ExamInviteBo;
import org.dromara.exam.manage.domain.vo.ExamInviteVo;
import org.dromara.exam.manage.domain.ExamInvite;
import org.dromara.exam.manage.mapper.ExamInviteMapper;
import org.dromara.exam.manage.service.IExamInviteService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 考试邀请记录Service业务层处理
 *
 * @author LionLi
 * @date 2026-09-30
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ExamInviteServiceImpl implements IExamInviteService {

    private final ExamInviteMapper baseMapper;

    /**
     * 查询考试邀请记录
     *
     * @param id 主键
     * @return 考试邀请记录
     */
    @Override
    public ExamInviteVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询考试邀请记录列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 考试邀请记录分页列表
     */
    @Override
    public TableDataInfo<ExamInviteVo> queryPageList(ExamInviteBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ExamInvite> lqw = buildQueryWrapper(bo);
        Page<ExamInviteVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的考试邀请记录列表
     *
     * @param bo 查询条件
     * @return 考试邀请记录列表
     */
    @Override
    public List<ExamInviteVo> queryList(ExamInviteBo bo) {
        LambdaQueryWrapper<ExamInvite> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ExamInvite> buildQueryWrapper(ExamInviteBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ExamInvite> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(ExamInvite::getId);
        lqw.eq(bo.getExamId() != null, ExamInvite::getExamId, bo.getExamId());
        lqw.eq(StringUtils.isNotBlank(bo.getInviteAccount()), ExamInvite::getInviteAccount, bo.getInviteAccount());
        lqw.eq(StringUtils.isNotBlank(bo.getInviteType()), ExamInvite::getInviteType, bo.getInviteType());
        lqw.eq(StringUtils.isNotBlank(bo.getInviteStatus()), ExamInvite::getInviteStatus, bo.getInviteStatus());
        lqw.eq(bo.getInviteTime() != null, ExamInvite::getInviteTime, bo.getInviteTime());
        return lqw;
    }

    /**
     * 新增考试邀请记录
     *
     * @param bo 考试邀请记录
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(ExamInviteBo bo) {
        ExamInvite add = MapstructUtils.convert(bo, ExamInvite.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改考试邀请记录
     *
     * @param bo 考试邀请记录
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(ExamInviteBo bo) {
        ExamInvite update = MapstructUtils.convert(bo, ExamInvite.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ExamInvite entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除考试邀请记录信息
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
