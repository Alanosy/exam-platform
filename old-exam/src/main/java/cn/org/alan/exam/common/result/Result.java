package cn.org.alan.exam.common.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 标准响应结构体
 *
 * @author haoxr
 * @since 2022/1/30
 **/
@Data
@ApiModel("标准响应结构体")
public class Result<T> implements Serializable {

    @ApiModelProperty("状态码")
    private Integer code;

    @ApiModelProperty("响应数据")
    private T data;

    @ApiModelProperty("响应消息")
    private String msg;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(String msg,T data) {
        Result<T> result = new Result<>();
        result.setCode(1);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(String msg) {
        Result<T> result = new Result<>();
        result.setCode(1);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }

    public static <T> Result<T> failed(String msg) {
        return result(0,msg , null);
    }

    private static <T> Result<T> result(Integer code, String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setData(data);
        result.setMsg(msg);
        return result;
    }
}
