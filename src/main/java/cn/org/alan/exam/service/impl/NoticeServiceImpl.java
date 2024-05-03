package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.NoticeConverter;
import cn.org.alan.exam.mapper.NoticeMapper;
import cn.org.alan.exam.model.entity.Notice;
import cn.org.alan.exam.model.form.NoticeForm;
import cn.org.alan.exam.model.vo.NoticeVO;
import cn.org.alan.exam.service.INoticeService;
import cn.org.alan.exam.util.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

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

    @Override
    public Result<String> addNotice(NoticeForm noticeForm) {
        // 设置创建人
        noticeForm.setUserId(SecurityUtil.getUserId());
        // 添加公告
        int rowsAffected = noticeMapper.insert(noticeConverter.formToEntity(noticeForm));
        if (rowsAffected == 0) {
            return Result.failed("添加失败");
        }
        return Result.success("添加成功");
    }

    @Override
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
        return Result.success("删除成功");
    }

    @Override
    public Result<String> updateNotice(String id, NoticeForm noticeForm) {
        // 创建更新条件
        LambdaUpdateWrapper<Notice> noticeWrapper = new LambdaUpdateWrapper<>();
        noticeWrapper.eq(Notice::getId, id)
                .set(Notice::getContent, noticeForm.getContent());
        // 更新公告
        int rowsAffected = noticeMapper.update(noticeWrapper);
        if (rowsAffected == 0) {
            return Result.failed("修改失败");
        }
        return Result.success("修改成功");
    }

    @Override
    public Result<IPage<NoticeVO>> getNotice(Integer pageNum, Integer pageSize, String title) {
        // 创建分页对象
        Page<Notice> page = new Page<>(pageNum, pageSize);
        // 创建查询条件
        LambdaQueryWrapper<Notice> noticeQueryWrapper = new LambdaQueryWrapper<>();
        noticeQueryWrapper.like(StringUtils.isNotBlank(title), Notice::getTitle, title)
                .eq(Notice::getUserId, SecurityUtil.getUserId());
        // 教师分页查询公告
        Page<Notice> gradePage = noticeMapper.selectPage(page, noticeQueryWrapper);
        return Result.success("查询成功", noticeConverter.pageEntityToVo(gradePage));
    }

    @Override
    public Result<IPage<NoticeVO>> getNewNotice(Integer pageNum, Integer pageSize) {
        // 创建分页对象
        Page<Notice> page = new Page<>(pageNum, pageSize);
        // 创建查询条件
        LambdaQueryWrapper<Notice> noticeQueryWrapper = new LambdaQueryWrapper<>();
        noticeQueryWrapper.eq(Notice::getUserId, SecurityUtil.getUserId())
                .orderByDesc(Notice::getCreateTime);
        // 学生分页查询公告
        Page<Notice> gradePage = noticeMapper.selectPage(page, noticeQueryWrapper);
        return Result.success("查询成功", noticeConverter.pageEntityToVo(gradePage));
    }
}
