package cn.org.alan.exam.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结构体
 *
 * @author haoxr
 * @since 2022/1/30
 **/
@Data
public class Result<T> implements Serializable {

    private Integer code;

    private T data;

    private String msg;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(1);
        result.setMsg("请求成功！");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> failed(ResultCode resultCode) {
        return result(0,resultCode.getMsg() , null);
    }

//    public static <T> Result<T> failed(String msg) {
//        return result(ResultCode.SYSTEM_EXECUTION_ERROR.getCode(), msg, null);
//    }
//
//    public static <T> Result<T> judge(boolean status) {
//        if (status) {
//            return success();
//        } else {
//            return failed();
//        }
//    }
//
//    public static <T> Result<T> failed(IResultCode resultCode) {
//        return result(resultCode.getCode(), resultCode.getMsg(), null);
//    }
//
//    public static <T> Result<T> failed(IResultCode resultCode, String msg) {
//        return result(resultCode.getCode(), msg, null);
//    }
//
//    private static <T> Result<T> result(IResultCode resultCode, T data) {
//        return result(resultCode.getCode(), resultCode.getMsg(), data);
//    }

    private static <T> Result<T> result(Integer code, String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setData(data);
        result.setMsg(msg);
        return result;
    }

//    public static boolean isSuccess(Result<?> result) {
//        return result != null && ResultCode.SUCCESS.getCode().equals(result.getCode());
//    }
}
