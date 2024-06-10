package cn.org.alan.exam.util;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.org.alan.exam.model.entity.RedisData;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @Author Alan
 * @Version
 * @Date 2024/6/9 11:03 PM
 */
@Slf4j
@Component
public class CacheClient {

    // 注入StringRedisTemplate
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    private Random random = new Random();
    // 方法1 解决穿透
    public void set(String key, Object value, Long time, TimeUnit unit) {
        // 我们往Redis存的时候不能是Object类型，我们需要把Object序列化为JSON字符串
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    // 方法2 解决击穿   使用逻辑过期  比上面的方法的操作多了一个逻辑过期字段而已
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        // 设置逻辑过期
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        // 我们往Redis存的时候不能是Object类型，我们需要把Object序列化为JSON字符串
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    // 方法3 解决穿透
    // 返回值不确定，我们要使用泛型,比如<R>R,具体是什么类型由用户传入Class<R> type
    public <R, ID, T> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Class<T> voClass, Long time, TimeUnit unit) {

        String key = keyPrefix + id;

        // TODO 1. 从Redis查询商铺缓存
        // 可以选择Hash结构，没问题，也能String
        String json = stringRedisTemplate.opsForValue().get(key);

        // TODO 2. 判断时Redis是否命中
        if (StrUtil.isNotBlank(json)) {
            if (type.equals(IPage.class)) {
                // List<T> dataList = JSONUtil.toList(JSONUtil.parseArray(), voClass);
                // IPage<T> page = new Page<>();
                // page.setRecords(dataList); // 假设Page类有此方法设置数据列表
                // // 设置其他分页属性，如total、current等，这取决于IPage的具体实现
                // return (R) page; // 强制转换为 R 类型返回

                // 解析JSON
                JSONObject jsonObject = JSONUtil.parseObj(json);

                // 获取记录列表并转换为List<T>
                List<T> dataList = JSONUtil.toList(jsonObject.getJSONArray("records"), voClass);

                // 创建Page对象并设置属性
                IPage<T> page = new Page<>();
                page.setRecords(dataList); // 设置记录列表
                page.setTotal(jsonObject.getInt("total")); // 设置总记录数
                page.setSize(jsonObject.getInt("size")); // 设置每页大小
                page.setCurrent(jsonObject.getInt("current")); // 设置当前页码

                return (R) page; // 强制转换为 R 类型返回
            } else if(type.equals(List.class)){
                // TODO 3. 存在，返回商户信息
                List<T> gradeVOList = JSONUtil.toList(json, voClass);
                return (R)gradeVOList;
            }else{
                return JSONUtil.toBean(json, type);
            }


        }
        // TODO 多判断一步，命中的是否是空值
        // 运行到这里，说明上面的if没有进去，->说明StrUtil.isNotBlank(shopJson)是false ->shopJson两种情况 空白字符串或者null
        if (json != null) {
            // 不能等于null，就一定是一个空字符串
            return null;
        }


        //      TODO 4. 不存在，向数据库进行查询
        R r = dbFallback.apply(id);
        //      TODO 5. 数据库不存在，返回错误
        if (r == null) {
            //          将空值写入redis
            stringRedisTemplate.opsForValue().set(key, "", 2, TimeUnit.MINUTES);
            //          返回错误信息
            return null;
        }

        //      TODO 6. 存在,写入Redis
        String shopTOJson = JSONUtil.toJsonStr(r);
        stringRedisTemplate.opsForValue().

                set(key, shopTOJson, time+random.nextInt(20), unit);

        //      TODO 7. 返回最终结果
        return r;
    }


    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);
    ;

    //  方法4：解决缓存击穿
    public <R, ID> R queryWithLogicalExpire(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;

        //      TODO 1.从redis查询商铺缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        //      TODO 2. 判断时Redis是否命中
        if (StrUtil.isBlank(json)) {
            //  TODO 3.缓存不存在，直接返回空
            return null;
        }
        //      TODO 4.存在,需要先把JSON反序列化为对象
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        //      因为我们在RedisData中设置data属性就是Object类型，所以当我们取的时候程序并不知道我们是什么类型，我们加一个强转就好了
        JSONObject shopData = (JSONObject) redisData.getData();
        R r = JSONUtil.toBean(shopData, type);
        LocalDateTime expireTime = redisData.getExpireTime();

        //      TODO 5.判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            //          TODO 5.1 未过期,返回商铺信息
            return r;
        }

        //      TODO 5.2 已过期,需要缓存重建
        //      TODO 6.  缓存重建
        //      TODO 6.1 获取互斥锁
        String lockKey = "lock:shop:" + id;
        boolean isLock = tryLock(lockKey);

        //      TODO 6.2 判断是否获取锁成功
        if (isLock) {
            //      TODO 6.3 成功，获取锁成功应该再次检测Redis缓存是否过期，做DoubleCheck，如果存在则无序重建缓存
            json = stringRedisTemplate.opsForValue().get("cache:shop:" + id);
            redisData = JSONUtil.toBean(json, RedisData.class);
            shopData = (JSONObject) redisData.getData();
            r = JSONUtil.toBean(shopData, type);
            expireTime = redisData.getExpireTime();
            if (expireTime.isAfter(LocalDateTime.now())) {
                //          TODO 未过期,返回商铺信息
                return r;
            }

            //       TODO 成功,但是缓存过期了，开启独立线程,实现缓存重建（建议使用线程池）
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    // TODO 重建缓存  先查数据库，再写入Redis
                    R r1 = dbFallback.apply(id);
                    // TODO 写入缓存要带有逻辑过期
                    this.setWithLogicalExpire(key, r1, time+random.nextInt(20), unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    //                  释放锁
                    unlock(lockKey);
                }
            });

        }
        //      TODO 6.4 失败,返回已经过期的商品信息
        return r;
    }

    //    拿到锁
    private boolean tryLock(String key) {
        // setIfAbsent方法就是Redis中的setnx
        // 在Redis命令行中的运行结果就是0或者1，但是在这的运行结果是true或false，但是返回的是Boolean类型，封装类
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        // 不建议直接返回：会自动拆箱，有时候会出现空指针
        return BooleanUtil.isTrue(flag);
    }

    //    释放锁
    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }



}
