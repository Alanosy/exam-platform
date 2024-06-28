package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.NoticeConverter;
import cn.org.alan.exam.mapper.GradeMapper;
import cn.org.alan.exam.mapper.NoticeGradeMapper;
import cn.org.alan.exam.mapper.NoticeMapper;
import cn.org.alan.exam.model.entity.Grade;
import cn.org.alan.exam.model.entity.Notice;
import cn.org.alan.exam.model.entity.NoticeGrade;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.form.NoticeForm;
import cn.org.alan.exam.model.vo.GradeVO;
import cn.org.alan.exam.model.vo.NoticeVO;
import cn.org.alan.exam.service.INoticeService;
import cn.org.alan.exam.util.CacheClient;
import cn.org.alan.exam.util.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 公告实现类
 *
 * @author Alan
 * @since 2024-03-21
 */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements INoticeService {
    @Resource
    private NoticeMapper noticeMapper;
    @Resource
    private NoticeConverter noticeConverter;
    @Resource
    private NoticeGradeMapper noticeGradeMapper;
    @Resource
    private GradeMapper gradeMapper;
    @Resource
    private CacheClient cacheClient;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional
    public Result<String> addNotice(NoticeForm noticeForm) {
        // 设置创建人
        noticeForm.setUserId(SecurityUtil.getUserId());
        // 添加公告
        Notice notice = noticeConverter.formToEntity(noticeForm);
        int rowsAffected = noticeMapper.insert(notice);

        if (rowsAffected == 0) {
            return Result.failed("添加失败");
        }
        LambdaQueryWrapper<Grade> gradeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        gradeLambdaQueryWrapper.eq(Grade::getUserId,SecurityUtil.getUserId());
        List<Grade> grades = gradeMapper.selectList(gradeLambdaQueryWrapper);
        Integer noticeId = notice.getId();
        if(grades.isEmpty()){
            return  Result.failed("您还没有创建班级，请先创建班级后再添加公告尝试");
        }

        int addNoticeGradeRow = noticeGradeMapper.addNoticeGrade(noticeId,grades);
        if (addNoticeGradeRow == 0) {
            return Result.failed("添加失败");
        }
        // 更新缓存
        if (notice.getId() != null) { // 确保ID有效
            // 如果是更新操作，先从缓存中移除旧数据，然后重新放入最新的数据
            stringRedisTemplate.delete("cache:notice:getNotice:"+notice.getId().toString()); // 删除旧缓存
            stringRedisTemplate.delete("cache:notice:getNewNotice:"+notice.getId().toString());
            // NoticeVO updatedNoticeVO = noticeConverter.NoticeToNoticeVO(notice); // 转换为视图对象
            // Map<Integer, NoticeVO> map = Map.of(updatedNoticeVO.getId(), updatedNoticeVO);
            // cacheClient.batchPut("cache:notice:getNotice:",map,10L,TimeUnit.MINUTES); // 存储新数据
            // cacheClient.batchPut("cache:notice:getNewNotice:",map,10L,TimeUnit.MINUTES); // 存储新数据

        }
        return Result.success("添加成功");
    }

    @Override
    @Transactional
    public Result<String> deleteNotice(String ids) {
        // 转换为集合
        List<Integer> noticeIds = Arrays.stream(ids.split(","))
                .map(Integer::parseInt)
                .toList();
        // 删除公告
        int rowsAffected = noticeMapper.removeNotice(noticeIds);
        if (rowsAffected == 0) {
            return Result.failed("删除失败");
        }
        noticeIds.forEach(id->{
            stringRedisTemplate.delete("cache:notice:getNewNotice:"+id);
            stringRedisTemplate.delete("cache:notice:getNotice:"+id);
        });

        return Result.success("删除成功");
    }

    @Override
    @Transactional
    public Result<String> updateNotice(String id, NoticeForm noticeForm) {
        // 创建更新条件
        LambdaUpdateWrapper<Notice> noticeWrapper = new LambdaUpdateWrapper<>();
        noticeWrapper.eq(Notice::getId, id)
                .eq(Notice::getIsDeleted,0)
                .set(Notice::getContent, noticeForm.getContent());
        // 更新公告
        int rowsAffected = noticeMapper.update(noticeWrapper);
        if (rowsAffected == 0) {
            return Result.failed("修改失败");
        }
        Notice byId = getById(id);
        if (byId.getId() != null) { // 确保ID有效
            // 如果是更新操作，先从缓存中移除旧数据，然后重新放入最新的数据
            stringRedisTemplate.delete("cache:notice:getNotice:"+byId.getId().toString()); // 删除旧缓存
            stringRedisTemplate.delete("cache:notice:getNewNotice:"+byId.getId().toString()); // 删除旧缓存
            // NoticeVO updatedNoticeVO = noticeConverter.NoticeToNoticeVO(byId); // 转换为视图对象
            // Map<Integer, NoticeVO> map = Map.of(updatedNoticeVO.getId(), updatedNoticeVO);
            // cacheClient.batchPut("cache:notice:getNotice:",map,10L,TimeUnit.MINUTES); // 存储新数据
            // cacheClient.batchPut("cache:notice:getNewNotice:",map,10L,TimeUnit.MINUTES); // 存储新数据
        }
        return Result.success("修改成功");
    }

    @Override
    public Result<IPage<NoticeVO>> getNotice(Integer pageNum, Integer pageSize, String title) {
        // 查询满足条件的总记录数
        int total = noticeMapper.countByCondition(SecurityUtil.getUserId(), title); // 假设gradeMapper中实现了根据条件计数的方法
        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;

        // 查询分页ID列表
        List<Integer> noticeIds = noticeMapper.selectNoticeIdsPage(SecurityUtil.getUserId(), title, offset, pageSize);

        // 批量从缓存中获取GradeVO对象
        Map<Integer, NoticeVO> cachedGradesMap = cacheClient.batchGet("cache:notice:getNotice:",noticeIds, NoticeVO.class);

        // 确定未命中的ID列表
        List<Integer> missIds = new ArrayList<>();
        for (Integer id : noticeIds) {
            if (!cachedGradesMap.containsKey(id)) {
                missIds.add(id);
            }
        }

        // 如果有未命中的ID，从数据库批量查询并更新缓存
        if (!missIds.isEmpty()) {
            List<NoticeVO> missedGrades = noticeMapper.batchSelectByIds(missIds);
            // 假设GradeVO的ID为getId()，使用Collectors.toMap转换
            Map<Integer, NoticeVO> missedGradesMap = missedGrades.stream()
                    .collect(Collectors.toMap(NoticeVO::getId, Function.identity()));
            // 更新缓存
            cacheClient.batchPut("cache:notice:getNotice:",missedGradesMap,10L, TimeUnit.MINUTES);
            // 合并缓存结果
            cachedGradesMap.putAll(missedGradesMap);
        }

        // 根据ID列表从缓存中获取完整的GradeVO对象列表
        List<NoticeVO> finalResult = new ArrayList<>(noticeIds.size());
        for (Integer id : noticeIds) {
            finalResult.add(cachedGradesMap.get(id));
        }

        // 构建并返回IPage对象
        IPage<NoticeVO> resultPage = new Page<>(pageNum, pageSize, Long.valueOf(total));
        resultPage.setRecords(finalResult);

        return Result.success("查询成功", resultPage);

    }

    @Override
    public Result<IPage<NoticeVO>> getNewNotice(Integer pageNum, Integer pageSize) {
        // 创建分页对象
        Page<NoticeVO> page = new Page<>(pageNum, pageSize);
        // 学生分页查询公告
         page = noticeMapper.selectNewNoticePage(page,SecurityUtil.getUserId());
        // return Result.success("查询成功", page);
        // 查询满足条件的总记录数
        int total = noticeMapper.countByCondition(SecurityUtil.getUserId(), null); // 假设gradeMapper中实现了根据条件计数的方法
        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;

        // 查询分页ID列表
        List<Integer> noticeIds = noticeMapper.selectNewNoticeIdsPage(SecurityUtil.getUserId(), offset, pageSize);

        // 批量从缓存中获取GradeVO对象
        Map<Integer, NoticeVO> cachedGradesMap = cacheClient.batchGet("cache:notice:getNewNotice:",noticeIds, NoticeVO.class);

        // 确定未命中的ID列表
        List<Integer> missIds = new ArrayList<>();
        for (Integer id : noticeIds) {
            if (!cachedGradesMap.containsKey(id)) {
                missIds.add(id);
            }
        }

        // 如果有未命中的ID，从数据库批量查询并更新缓存
        if (!missIds.isEmpty()) {
            List<NoticeVO> missedGrades = noticeMapper.batchSelectByIds(missIds);
            // 假设GradeVO的ID为getId()，使用Collectors.toMap转换
            Map<Integer, NoticeVO> missedGradesMap = missedGrades.stream()
                    .collect(Collectors.toMap(NoticeVO::getId, Function.identity()));
            // 更新缓存
            cacheClient.batchPut("cache:notice:getNotice:",missedGradesMap,10L, TimeUnit.MINUTES);
            // 合并缓存结果
            cachedGradesMap.putAll(missedGradesMap);
        }

        // 根据ID列表从缓存中获取完整的GradeVO对象列表
        List<NoticeVO> finalResult = new ArrayList<>(noticeIds.size());
        for (Integer id : noticeIds) {
            finalResult.add(cachedGradesMap.get(id));
        }

        // 构建并返回IPage对象
        IPage<NoticeVO> resultPage = new Page<>(pageNum, pageSize, Long.valueOf(total));
        resultPage.setRecords(finalResult);

        return Result.success("查询成功", resultPage);
    }
}
