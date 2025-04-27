package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.Reply;
import cn.org.alan.exam.model.vo.reply.ReplyVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author WeiJin
 * @version 1.0
 * @since 2025/4/4 13:21
 */
@Mapper
public interface ReplyMapper extends BaseMapper<Reply> {
    /**
     * 根据讨论id获取回复
     *
     * @param orderBy      排序方式 1时间升序 2时间降序 3点赞数量升序 4点赞数量降序
     * @param discussionId 讨论id
     * @param userId       当前用户id
     * @return 回复
     */
    List<ReplyVo> selectReplies(Integer discussionId, Integer userId, Integer orderBy);

    /**
     * 获取子评论
     *
     * @param replyId 父评论id
     * @return 所有子评论
     */
    List<ReplyVo> selectChildReplies(Integer replyId);

}
