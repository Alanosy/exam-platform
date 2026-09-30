package cn.org.alan.exam.service;

import cn.org.alan.exam.model.entity.Log;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 说明：
 * 日志记录服务（主要记录登录日志）
 *
 * @Author Alan
 * @Version 1.0
 * @Date 2025/4/4 11:38 AM
 */
public interface ILogService {
    /**
     * 记录登录日志
     * @param log
     * @return
     */
    Log add(Log log);

    /**
     * 分页查询登录日志
     * @param pageNum
     * @param pageSize
     * @return
     */
    Page<Log> getPage(Integer pageNum, Integer pageSize);
}
