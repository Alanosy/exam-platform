package cn.org.alan.exam.model.entity;

public class ResponsePojo<T> {
    private T data;
    private String message;
    private int code; //成功为1，失败为2

    private ResponsePojo(T t) {
        this.data = t;
        this.code = 1;
        this.message = "";
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    private ResponsePojo(T t, String message, int code) {
        this.data = t;
        this.code = code;
        this.message = message;
    }

    public static <T> ResponsePojo<T> success(T t) {
        return new ResponsePojo(t);
    }

    public static <T> ResponsePojo<T> fail(T t, String message) {
        return new ResponsePojo(t, message, 2);
    }

}
