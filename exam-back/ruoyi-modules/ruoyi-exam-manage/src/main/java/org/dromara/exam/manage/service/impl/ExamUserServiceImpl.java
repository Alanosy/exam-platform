package org.dromara.exam.manage.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.exam.manage.domain.ExamUser;
import org.dromara.exam.manage.domain.bo.ExamUserBo;
import org.dromara.exam.manage.domain.vo.ExamWhiteUserVo;
import org.dromara.exam.manage.mapper.ExamMapper;
import org.dromara.exam.manage.mapper.ExamUserMapper;
import org.dromara.exam.manage.service.IExamUserService;
import org.dromara.system.api.RemoteDeptService;
import org.dromara.system.api.RemoteUserService;
import org.dromara.system.api.domain.vo.RemoteUserVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 考试白名单考生Service业务层处理
 *
 * @author LionLi
 * @date 2026-10-02
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ExamUserServiceImpl implements IExamUserService {

    private final ExamUserMapper baseMapper;

    private final ExamMapper examMapper;

    @DubboReference
    private RemoteUserService remoteUserService;

    @DubboReference
    private RemoteDeptService remoteDeptService;

    /**
     * 查询某场考试的白名单考生
     */
    @Override
    public List<ExamWhiteUserVo> queryWhiteUsers(Long examId) {
        List<Long> userIds = queryWhiteUserIds(examId);
        if (CollUtil.isEmpty(userIds)) {
            return List.of();
        }
        List<RemoteUserVo> users = remoteUserService.selectListByIds(userIds);
        if (CollUtil.isEmpty(users)) {
            return List.of();
        }
        // 部门名一次查回来再分发，避免一行一次远程调用
        List<Long> deptIds = users.stream().map(RemoteUserVo::getDeptId).filter(ObjectUtil::isNotNull).distinct().toList();
        Map<Long, String> deptNames = CollUtil.isEmpty(deptIds) ? Map.of() : remoteDeptService.selectDeptNamesByIds(deptIds);
        if (ObjectUtil.isNull(deptNames)) {
            deptNames = Map.of();
        }
        Map<Long, String> finalDeptNames = deptNames;
        return users.stream().map(user -> {
            ExamWhiteUserVo vo = new ExamWhiteUserVo();
            vo.setUserId(user.getUserId());
            vo.setUserName(user.getUserName());
            vo.setNickName(user.getNickName());
            vo.setDeptId(user.getDeptId());
            vo.setDeptName(ObjectUtil.isNull(user.getDeptId()) ? null : finalDeptNames.get(user.getDeptId()));
            vo.setPhonenumber(user.getPhonenumber());
            return vo;
        }).toList();
    }

    /**
     * 查询某场考试的白名单考生ID
     */
    @Override
    public List<Long> queryWhiteUserIds(Long examId) {
        if (ObjectUtil.isNull(examId)) {
            return List.of();
        }
        List<ExamUser> list = baseMapper.selectList(
            Wrappers.lambdaQuery(ExamUser.class)
                .select(ExamUser::getUserId)
                .eq(ExamUser::getExamId, examId));
        // 逻辑删除的行已被 @TableLogic 过滤，这里的 distinct 只是防脏数据重复占位
        return list.stream().map(ExamUser::getUserId).filter(ObjectUtil::isNotNull).distinct().toList();
    }

    /**
     * 整体覆盖某场考试的白名单
     *
     * <p>按「新名单 vs 旧名单」做增量：旧的有、新的没有才删；新的有、旧的没有才插。
     * 直接全删再全插会把一次「移出一个人」变成几百条变更记录。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWhiteUsers(ExamUserBo bo) {
        Long examId = bo.getExamId();
        if (ObjectUtil.isNull(examId)) {
            throw new ServiceException("考试ID不能为空");
        }
        if (ObjectUtil.isNull(examMapper.selectById(examId))) {
            throw new ServiceException("考试不存在或已删除");
        }
        List<Long> target = CollUtil.isEmpty(bo.getUserIds())
            ? List.of()
            : bo.getUserIds().stream().filter(ObjectUtil::isNotNull).distinct().toList();
        Set<Long> oldIds = new HashSet<>(queryWhiteUserIds(examId));
        Set<Long> newIds = new HashSet<>(target);

        List<Long> removeIds = oldIds.stream().filter(id -> !newIds.contains(id)).toList();
        List<Long> addIds = newIds.stream().filter(id -> !oldIds.contains(id)).toList();
        if (CollUtil.isNotEmpty(removeIds)) {
            LambdaQueryWrapper<ExamUser> dlqw = Wrappers.lambdaQuery();
            dlqw.eq(ExamUser::getExamId, examId).in(ExamUser::getUserId, removeIds);
            baseMapper.delete(dlqw);
        }
        for (Long userId : addIds) {
            ExamUser add = new ExamUser();
            add.setExamId(examId);
            add.setUserId(userId);
            baseMapper.insert(add);
        }
        log.info("保存考试白名单：examId={}，新增 {} 人，移出 {} 人，合计 {} 人", examId, addIds.size(), removeIds.size(), newIds.size());
    }

    /**
     * 按考试删除白名单
     */
    @Override
    public void deleteByExamIds(Collection<Long> examIds) {
        if (CollUtil.isEmpty(examIds)) {
            return;
        }
        LambdaQueryWrapper<ExamUser> lqw = Wrappers.lambdaQuery();
        lqw.in(ExamUser::getExamId, examIds);
        baseMapper.delete(lqw);
    }

    /**
     * 批量统计考试的应考人数
     */
    @Override
    public Map<Long, Long> countMapByExamIds(List<Long> examIds) {
        Map<Long, Long> map = new HashMap<>();
        if (CollUtil.isEmpty(examIds)) {
            return map;
        }
        QueryWrapper<ExamUser> qw = Wrappers.query();
        qw.select(true, List.of("exam_id", "count(1) as cnt"));
        qw.in("exam_id", examIds);
        qw.groupBy("exam_id");
        List<Map<String, Object>> rows = baseMapper.selectMaps(qw);
        if (CollUtil.isEmpty(rows)) {
            return map;
        }
        for (Map<String, Object> row : rows) {
            // MyBatis 对 Map 结果的 key 依赖 map-underscore-to-camel-case 配置，两种写法都兜住
            Object examId = ObjectUtil.isNotNull(row.get("examId")) ? row.get("examId") : row.get("exam_id");
            Object cnt = row.get("cnt");
            if (ObjectUtil.isNotNull(examId) && ObjectUtil.isNotNull(cnt)) {
                map.put(Long.valueOf(String.valueOf(examId)), Long.valueOf(String.valueOf(cnt)));
            }
        }
        return map;
    }
}
