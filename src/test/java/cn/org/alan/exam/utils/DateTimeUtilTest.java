package cn.org.alan.exam.utils;

import cn.org.alan.exam.mapper.UserMapper;
import cn.org.alan.exam.model.entity.User;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author Alan
 * @Version
 * @Date 2024/5/28 11:28 PM
 */ class DateTimeUtilTest {

     @Resource
     private UserMapper userMapper;
    @Test
    void getDate() {
        List<User> users = userMapper.selectList(null);
        System.out.println(users);
        // LocalDate date = DateTimeUtil.getDate();
        // System.out.println(date);
    }

    @Test
    void dateToStr() {
        System.out.println(DateTimeUtil.dateToStr(DateTimeUtil.getDate()));
    }
}