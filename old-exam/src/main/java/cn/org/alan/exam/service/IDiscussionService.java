package cn.org.alan.exam.service;

import cn.org.alan.exam.model.entity.Discussion;
import cn.org.alan.exam.model.form.discussion.DiscussionForm;
import cn.org.alan.exam.model.vo.discussion.DiscussionDetailVo;
import cn.org.alan.exam.model.vo.discussion.PageDiscussionVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 说明：
 * 讨论服务接口
 *
 * @Author Alan
 * @Version 1.0
 * @Date 2025/4/2 6:35 PM
 */
public interface IDiscussionService extends IService<Discussion> {

    // ******* 教师讨论相关 *******

    // 创建讨论主题
    Discussion createDiscussion(DiscussionForm discussionForm);


    // 删除讨论主题
    int deleteDiscussion(Integer id);

    // 教师根据班级id或讨论标题获取自己发布的讨论(分页)
    Page<PageDiscussionVo> getOwnerDiscussions(String title, Integer gradeId, Integer currentPage, Integer size);

    // 获取讨论主题详情
    DiscussionDetailVo getDiscussionDetail(Integer id);

    // ******* 学生相关 *******


    /**
     * 学生根据班级id分页获取讨论
     *
     * @param title       标题
     * @param currentPage 当前页
     * @param size        每页记录数
     * @return 分页查询结果
     */
    Page<PageDiscussionVo> pageDiscussionByGrade(String title, Integer currentPage, Integer size);

}
