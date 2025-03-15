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
    List<Integer> getUserListByGradeId(Integer gradeId);

    Integer teacherExitClass(Integer userId, String gradeId);

    List<Integer> getGradeIdListByUserId(Integer userId);
}
