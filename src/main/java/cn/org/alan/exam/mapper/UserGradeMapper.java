package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.entity.UserGrade;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author Alan
 * @Version
 * @Date 2025/3/14 7:07 PM
 */
@Repository
public interface UserGradeMapper  extends BaseMapper<UserGrade> {
    /**
     * 根据班级id获取班级的所有老师
     * @param gradeId
     * @return
     */
    List<Integer> getUserListByGradeId(Integer gradeId);

    /**
     * 老师退出班级
     * @param userId
     * @param gradeId
     * @return
     */
    Integer teacherExitClass(Integer userId, String gradeId);

    /**
     * 教师id获取老师的所有班级id
     * @param userId
     * @return
     */
    List<Integer> getGradeIdListByUserId(Integer userId);

    /**
     * 逻辑删除教师与班级的关联
     * @param gradeId
     */
    void deleteUserGrade(Integer gradeId);
}
