package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.NoticeGrade;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * 公告班级关联 Mapper 接口
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface NoticeGradeMapper extends BaseMapper<NoticeGrade> {

    /**
     * 添加公告和班级关系
     *
     * @param noticeId    公告ID
     * @param gradeIdList 班级ID列表
     * @return 添加记录数
     */
    int addNoticeGrade(Integer noticeId, List<Integer> gradeIdList);

    /**
     * 删除公告班级关系
     *
     * @param noticeId 公告ID
     * @return 删除记录数
     */
    Integer delNoticeGrade(Integer noticeId);

    /**
     * 获得班级关联的公告id
     *
     * @param gradeId 公告ID
     * @return 结果集
     */
    List<Integer> getNoticeIdList(Integer gradeId);

    /**
     * 获得班级关联的班级id
     *
     * @param noticeId 公告ID
     * @return 结果集
     */
    List<Integer> getGradeList(Integer noticeId);

    /**
     * 删除公告和班级关联
     *
     * @param noticeIds 公告ID
     */
    void deleteNoticeGrade(List<Integer> noticeIds);

}
