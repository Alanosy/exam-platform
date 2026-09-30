package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.Discussion;
import cn.org.alan.exam.model.vo.discussion.DiscussionDetailVo;
import cn.org.alan.exam.model.vo.discussion.PageDiscussionVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author WeiJin
 * @version 1.0
 * @since 2025/4/3 9:43
 */
@Mapper
public interface DiscussionMapper extends BaseMapper<Discussion> {
    /**
     * 教师分页获取自己的发布的讨论
     *
     * @param page    分页信息
     * @param userId  教师id
     * @param title   讨论标题
     * @param gradeId 班级id
     * @return 分页查询结果
     */
    Page<PageDiscussionVo> selectOwnerPage(Page<PageDiscussionVo> page, Integer userId, String title, Integer gradeId);

    /**
     * 获取讨论详情
     *
     * @param id 讨论id
     * @return 讨论信息
     */
    DiscussionDetailVo selectDetail(Integer id);

    /**
     * 学生根据班级id分页查询讨论信息
     *
     * @param page    分页信息
     * @param title   标题
     * @param gradeId 班级id
     * @return 分页查询结果
     */
    Page<PageDiscussionVo> selectDiscussionByGradePage(Page<PageDiscussionVo> page, String title, Integer gradeId);
}
