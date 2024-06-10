package cn.org.alan.exam.util;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * @Author Alan
 * @Version
 * @Date 2024/6/10 9:40 AM
 */
@Component
public class RedisUtils {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    /**
     * 使用Lua脚本删除以指定前缀开头的所有Key。
     * @param keyPrefix 前缀字符串，例如 "cache:getNewNotice:"
     */
    public void deleteKeysByPrefix(String keyPrefix) {
        String luaScript = "local keys = redis.call('SCAN', 0, 'MATCH', ARGV[1]..'*', 'COUNT', 1000)\n" +
                "local count = 0\n" +
                "for i=1,#keys[2],1 do \n" +
                "   redis.call('DEL', keys[2][i]) \n" +
                "   count = count + 1 \n" +
                "end \n" +
                "return count";

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);

        // 执行Lua脚本并传递前缀作为参数
        Long deletedCount = stringRedisTemplate.execute(redisScript, Collections.singletonList(keyPrefix), keyPrefix);
        System.out.println("Deleted " + deletedCount + " keys with prefix: " + keyPrefix);
    }
}
