package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.Like;
import cn.org.alan.exam.model.form.like.LikeForm;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author WeiJin
 * @version 1.0
 * @since 2025/4/16 22:21
 */
public interface ILikeService extends IService<Like> {

    /**
     * 点赞或取消点赞
     *
     * @param likeForm 入参
     * @return 统一响应格式
     */
    Result<String> doLike(LikeForm likeForm);

    /**
     * 根据用户id，讨论id，回复id 查询点赞信息
     *
     * @param likeForm 入参
     * @return 点赞信息
     */
    Like queryLike(LikeForm likeForm);

    /**
     * 根据id删除点赞
     *
     * @param id id
     * @return 影响数据库记录条数
     */
    int deleteLike(Integer id);

    /**
     * 根据讨论id删除点赞
     *
     * @param discussionId 讨论id
     * @return 影响数据来记录条数
     */
    int deleteLikeByDiscussionId(Integer discussionId);

    /**
     * 根据回复id删除点赞
     *
     * @param replyId 回复id
     * @return 影响数据来记录条数
     */
    int deleteLikeByReplyId(Integer replyId);
}
