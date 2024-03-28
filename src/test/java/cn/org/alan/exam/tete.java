package cn.org.alan.exam;


import cn.org.alan.exam.mapper.UserMapper;
import cn.org.alan.exam.model.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author Alan
 * @Version
 * @Date 2024/3/25 5:26 PM
 */
@SpringBootTest
@Slf4j
public class tete {


    @Autowired
    private UserMapper userMapper;
    @Test
    void contextLoads() {
        // List<UserEntity> text = userMapper.getinfo(1);
        // System.out.println(text);
    }


    @Test
    void testTime(){
        LocalDateTime now = LocalDateTime.now();
        System.out.println(now);
    }
}
