package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.Grade;
import cn.org.alan.exam.model.entity.NoticeGrade;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author WeiJin
 * @since 2024-03-21
 */
public interface NoticeGradeMapper extends BaseMapper<NoticeGrade> {

    /**
     * 根据公告id删除公告与班级关联
     * @param noticeIds 公告id列表
     * @return 影响数据库记录数
     */
    Integer deleteByNoticeIds(List<Integer> noticeIds);

    int addNoticeGrade(Integer noticeId, List<Grade> grades);
}
