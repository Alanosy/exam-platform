package cn.org.alan.exam.controller;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.entity.Log;
import cn.org.alan.exam.service.ILogService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 说明：
 *
 * @Author Alan
 * @Version 1.0
 * @Date 2025/4/4 11:37 AM
 */
@RestController
@Api(tags = "日志记录接口")
@RequestMapping("/api/log")
public class LogController {
    @Autowired
    private ILogService logService;

    @GetMapping
    @ApiOperation("分页查询日志")
    Result getLogPage(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                      @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize){
        Page<Log> page = logService.getPage(pageNum, pageSize);
        return Result.success("分页查询日志成功",page);
    }
}
