package cn.org.alan.exam.common.handler;

/**
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/3/29 16:10
 */

import cn.org.alan.exam.common.exception.AppException;
import cn.org.alan.exam.common.result.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    /**
     * 处理自定义异常
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(AppException.class)
    public Result<String> handleAppException(AppException e){
        log.error(e.getMessage());
        return Result.failed(e.getMessage());
    }

    /**
     * 处理参数校验异常
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error(e.getMessage());
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return Result.failed(message);
    }

    /**
     * 处理唯一约束异常
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<String> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException e){
        log.error(e.getMessage());
        return Result.failed("重复");
    }

    /**
     * 处理消息不可读异常
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException e){
        log.error(e.getMessage());
        return Result.failed("请求参数异常");
    }

    /**
     * 处理主键冲突异常
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<String> handleDuplicateKeyException(DuplicateKeyException e){
        String name = e.getMessage().split(":")[2].split(" ")[3];
        log.error(e.getMessage());
        return Result.failed(name + "已存在");
    }

    /**
     * 处理约束违反异常
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler({ConstraintViolationException.class})
    public Result<String> handleConstraintViolationException(ConstraintViolationException e) {
        return Result.failed(e.getMessage());
    }


}
