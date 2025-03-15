package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.UserDailyLoginDuration;
import cn.org.alan.exam.model.entity.UserExerciseRecord;
import cn.org.alan.exam.model.vo.stat.DailyVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.time.LocalDate;
import java.util.List;

/**
 * @Author Alan
 * @Version
 * @Date 2024/5/28 10:47 PM
 */
public interface UserDailyLoginDurationMapper extends BaseMapper<UserDailyLoginDuration> {
    List<DailyVO>  getDaily(Integer userId);

    UserDailyLoginDuration getTodeyRecord(Integer userId, LocalDate date);
}
