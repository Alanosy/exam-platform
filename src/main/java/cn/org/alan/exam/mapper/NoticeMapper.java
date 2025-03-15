package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.Notice;
import cn.org.alan.exam.model.form.NoticeForm;
import cn.org.alan.exam.model.vo.NoticeVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface NoticeMapper extends BaseMapper<Notice> {

    int removeNotice(List<Integer> noticeIds);

    /**
     * 删除用户创建的公告
     * @param userIds 用户id列表
     * @return 影响记录数
     */
    Integer deleteByUserIds(List<Integer> userIds);

    /**
     * 根据用户id获取公告ids
     * @param userIds 用户id列表
     * @return 查询结果
     */
    List<Integer> selectIdsByUserIds(List<Integer> userIds);

    Page<NoticeVO> selectNewNoticePage(Page<NoticeVO> page, Integer userId);

    int countByCondition(Integer userId, String title);

    List<Integer> selectNoticeIdsPage(Integer userId, String title, int offset, Integer pageSize);

    List<NoticeVO> batchSelectByIds(List<Integer> missIds);

    List<Integer> selectNewNoticeIdsPage(Integer userId, int offset, Integer pageSize);

    Integer getIsPublic(Integer noticeId);

    Integer updateNotice(Integer noticeId, NoticeForm noticeForm);

    Page<NoticeVO> getNewNotice(Page<NoticeVO> page, List<Integer> teachIdList, List<Integer> noticeIdList, List<Integer> adminIdList);

    List<NoticeVO> getNotice( Integer userId, String title);
}
