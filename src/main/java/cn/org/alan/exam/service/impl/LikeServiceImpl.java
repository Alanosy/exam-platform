package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.converter.LikeConverter;
import cn.org.alan.exam.mapper.LikeMapper;
import cn.org.alan.exam.model.entity.Like;
import cn.org.alan.exam.model.form.like.LikeForm;
import cn.org.alan.exam.service.ILikeService;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author WeiJin
 * @version 1.0
 * @since 2025/4/16 22:25
 */
@Service
public class LikeServiceImpl extends ServiceImpl<LikeMapper, Like> implements ILikeService {

    @Resource
    private LikeConverter likeConverter;

    @Override
    public Result<String> doLike(LikeForm likeForm) {
        Like like = likeConverter.formToEntity(likeForm);
        Like dataLike = queryLike(likeForm);
        if (dataLike == null) {
            // 新增
            like.setUserId(SecurityUtil.getUserId());
            int inserted = baseMapper.insert(like);
            return inserted > 0 ? Result.success("点赞成功") : Result.failed("点赞失败");
        }
        // 删除
        int deleted = deleteLike(dataLike.getId());
        return deleted > 0 ? Result.success("取消成功") : Result.failed("取消失败");
    }

    @Override
    public Like queryLike(LikeForm likeForm) {
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<Like>()
                .eq(Like::getUserId, SecurityUtil.getUserId())
                .eq(Like::getDiscussionId, likeForm.getDiscussionId())
                .eq(Like::getReplyId, likeForm.getReplyId());
        return baseMapper.selectOne(wrapper);
    }

    @Override
    public int deleteLike(Integer id) {
        return baseMapper.deleteById(id);
    }

    @Override
    public int deleteLikeByDiscussionId(Integer discussionId) {
        LambdaUpdateWrapper<Like> wrapper = new LambdaUpdateWrapper<Like>().eq(Like::getDiscussionId, discussionId);
        return baseMapper.delete(wrapper);
    }

    @Override
    public int deleteLikeByReplyId(Integer replyId) {
        LambdaUpdateWrapper<Like> wrapper = new LambdaUpdateWrapper<Like>().eq(Like::getReplyId, replyId);
        return baseMapper.delete(wrapper);
    }
}
