package cn.org.alan.exam.common.exception;

/**
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/3/29 21:06
 */
public class AppException extends RuntimeException{
    private Integer code;
    private String msg;

    public AppException(String msg){
        this.code = 0;
        this.msg = msg;
    }
}
