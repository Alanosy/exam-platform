package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.Notice;
import cn.org.alan.exam.model.vo.NoticeVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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

    Page<NoticeVO> selectNewNoticePage(Page<NoticeVO> page, Integer gradeId);
}
