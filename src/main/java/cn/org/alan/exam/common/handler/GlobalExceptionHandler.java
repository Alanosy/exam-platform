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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理自定义异常
     *
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(AppException.class)
    public Result<String> handleAppException(AppException e) {
        log.error(e.getMessage(), e.getClass());
        return Result.failed(e.getLocalizedMessage());
    }

    /**
     * 处理参数校验异常
     *
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error(e.getMessage(), e.getClass());
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return Result.failed(message);
    }

    /**
     * 处理唯一约束异常
     *
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<String> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException e) {
        log.error(e.getMessage(), e.getClass());
        return Result.failed("重复");
    }


    /**
     * 处理请求参数无法解析异常
     *
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error(e.getMessage(), e.getClass());
        return Result.failed("请求参数无法解析");
    }

    /**
     * 处理请求参数缺失异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<String> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.error(e.getMessage(), e.getClass());
        return Result.failed(e.getParameterName() + "为必填项");
    }

    /**
     * 处理主键冲突异常
     *
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<String> handleDuplicateKeyException(DuplicateKeyException e) {
        String name = e.getMessage().split(":")[2].split(" ")[3];
        log.error(e.getMessage(), e.getClass());
        return Result.failed(name + "已存在");
    }

    /**
     * 处理无权限访问异常
     *
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<String> handleAccessDeniedException(AccessDeniedException e) {
        log.error(e.getMessage(), e.getClass());
        return Result.failed("你没有该资源的访问权限");
    }


    /**
     * 处理文件太大异常
     *
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<String> handlerMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error(e.getMessage(), e.getClass());
        return Result.failed("文件太大，最大上传5MB");
    }

    /**
     * 处理文件获取不到异常
     *
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public Result<String> handlerMissingServletRequestPartException(MissingServletRequestPartException e) {
        log.error(e.getMessage(), e.getClass());
        return Result.failed("没有获取到文件");
    }


    /**
     * 处理约束违反异常
     *
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<String> handleConstraintViolationException(ConstraintViolationException e) {
        log.error(e.getMessage(), e.getClass());
        return Result.failed(e.getMessage());
    }

    /**
     * 处理其他异常
     *
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error(e.getMessage(), e.getClass(), e.getCause());
        return Result.failed("未知异常");
    }
}
