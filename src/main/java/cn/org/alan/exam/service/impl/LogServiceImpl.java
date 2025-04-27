package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.mapper.LogMapper;
import cn.org.alan.exam.model.entity.Log;
import cn.org.alan.exam.service.ILogService;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 说明：
 *
 * @Author Alan
 * @Version 1.0
 * @Date 2025/4/4 11:41 AM
 */
@Service
public class LogServiceImpl implements ILogService {
    @Autowired
    private LogMapper logMapper;

    @Override
    public Log add(Log log) {
        int insert = logMapper.insert(log);
        if(insert>0){
            return log;
        }
        throw new ServiceRuntimeException("添加日志失败");
    }

    @Override
    public Page<Log> getPage(Integer pageNum, Integer pageSize) {
        Integer userId = SecurityUtil.getUserId();
        Page<Log> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Log> query = new LambdaQueryWrapper<>();
        query.eq(Log::getUserId,userId)
                .orderByDesc(Log::getCreateTime);
        page = logMapper.selectPage(page,query);
        return page;
    }
}
