package cn.org.alan.exam.common.exception;

/**
 * 说明：
 * 自定义服务异常类
 *
 * @Author Alan
 * @Version 1.0
 * @Date 2025/3/20 11:15 AM
 */
public class ServiceRuntimeException extends RuntimeException {
    /**
     * 自定义服务异常类构造器
     *
     * @param msg
     */
    public ServiceRuntimeException(String msg) {
        super(msg);
    }
}
