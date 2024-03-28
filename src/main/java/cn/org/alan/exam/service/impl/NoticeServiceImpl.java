package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.NoticeConverter;
import cn.org.alan.exam.mapper.NoticeMapper;
import cn.org.alan.exam.model.entity.Grade;
import cn.org.alan.exam.model.entity.Notice;
import cn.org.alan.exam.model.form.NoticeForm;
import cn.org.alan.exam.model.vo.NoticeVO;
import cn.org.alan.exam.service.INoticeService;
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
 * <p>
 *  服务实现类
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements INoticeService {
    @Resource
    private NoticeMapper noticeMapper;
    @Resource
    private NoticeConverter noticeConverter;
    @Override
    public Result<String> addNotice(Integer userId, NoticeForm noticeForm) {
        noticeForm.setUserId(userId);
        int rowsAffected = noticeMapper.insert(noticeConverter.formToEntity(noticeForm));
        if (rowsAffected == 0) {
            return Result.failed("添加失败");
        }
        return Result.success("添加成功");
    }

    @Override
    public Result<String> deleteNotice(String ids) {
        List<Integer> noticeIds = Arrays.stream(ids.split(","))
                .map(Integer::parseInt)
                .toList();
        int rowsAffected = noticeMapper.removeNotice(noticeIds);
        if (rowsAffected == 0) {
            return Result.failed("删除失败");
        }
        return Result.success("删除成功");
    }

    @Override
    public Result<String> updateNotice(String id, NoticeForm noticeForm) {
        LambdaUpdateWrapper<Notice> noticeLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        noticeLambdaUpdateWrapper.eq(Notice::getId,id)
                .set(Notice::getContent,noticeForm.getContent());
        int rowsAffected = noticeMapper.update(noticeLambdaUpdateWrapper);
        if (rowsAffected == 0) {
            return Result.failed("修改失败");
        }
        return Result.success("修改成功");
    }

    @Override
    public Result<IPage<NoticeVO>> getNotice(Integer userId, Integer pageNum, Integer pageSize, String title) {
        Page<Notice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notice> noticeQueryWrapper = new LambdaQueryWrapper<>();
        noticeQueryWrapper.like(StringUtils.isNotBlank(title), Notice::getTitle, title)
                .eq(Notice::getUserId,userId);
        Page<Notice> gradePage = noticeMapper.selectPage(page, noticeQueryWrapper);
        return Result.success("查询成功", noticeConverter.pageEntityToVo(gradePage));
    }
}
