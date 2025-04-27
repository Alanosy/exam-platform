package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.converter.ReplyConverter;
import cn.org.alan.exam.mapper.DiscussionMapper;
import cn.org.alan.exam.mapper.ReplyMapper;
import cn.org.alan.exam.model.entity.Discussion;
import cn.org.alan.exam.model.entity.Reply;
import cn.org.alan.exam.model.form.reply.ReplyForm;
import cn.org.alan.exam.model.vo.reply.ReplyVo;
import cn.org.alan.exam.service.ILikeService;
import cn.org.alan.exam.service.IReplyService;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author WeiJin
 * @version 1.0
 * @since 2025/4/4 14:13
 */
@Service
public class ReplyServiceImpl extends ServiceImpl<ReplyMapper, Reply> implements IReplyService {

    @Resource
    private DiscussionMapper discussionMapper;
    @Resource
    private ReplyConverter replyConverter;
    @Resource
    private ILikeService likeService;

    @Override
    public Reply addReply(ReplyForm replyForm) {
        // 类型转换
        Reply reply = replyConverter.formToEntity(replyForm);
        // 如果没有设置父回复，默认设置为-1
        if (reply.getParentId() == null) {
            reply.setParentId(-1);
        }
        // 添加当前用户Id
        reply.setUserId(SecurityUtil.getUserId());
        int inserted = baseMapper.insert(reply);
        if (inserted > 0) {
            return reply;
        }
        throw new RuntimeException("回复失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deleteReply(Integer id) {
        // 获取回复
        Reply reply = baseMapper.selectById(id);
        // 非空校验
        if (reply == null) {
            throw new RuntimeException("该id无对应的回复");
        }
        // 获取当前用户id
        Integer currentUserId = SecurityUtil.getUserId();
        // 回复人id不是当前用户
        if (!Objects.equals(reply.getUserId(), currentUserId)) {
            // 获取讨论
            Discussion discussion = discussionMapper.selectById(reply.getDiscussionId());
            // 既不是本人删除回复也不是发布讨论人删除回复，抛出异常
            if (discussion != null && !Objects.equals(discussion.getUserId(), currentUserId)) {
                throw new RuntimeException("无权删除他人回复");
            }
        }
        // 删除回复
        int deleted = baseMapper.deleteById(id);
        // // 删除子回复
        // LambdaUpdateWrapper<Reply> wrapper = new LambdaUpdateWrapper<Reply>().eq(Reply::getParentId, id);
        // deleted += baseMapper.delete(wrapper);
        // 删除回复的点赞
        deleted += likeService.deleteLikeByReplyId(id);
        if (deleted > 0) {
            return deleted;
        }
        throw new RuntimeException("删除回复失败");
    }

    @Override
    public List<ReplyVo> queryReplyByDiscussionId(Integer orderBy, Integer discussionId) {
        return baseMapper.selectReplies(discussionId, SecurityUtil.getUserId(), orderBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByDiscussionId(Integer discussionId) {
        // 删除回复
        LambdaUpdateWrapper<Reply> wrapper = new LambdaUpdateWrapper<Reply>()
                .eq(Reply::getDiscussionId, discussionId);
        // 删除回复的点赞
        int count = likeService.deleteLikeByDiscussionId(discussionId);
        count += baseMapper.delete(wrapper);
        return count;
    }


}
