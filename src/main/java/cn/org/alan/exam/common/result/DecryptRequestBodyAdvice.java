package cn.org.alan.exam.common.result;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

/**
 * @Author Alan
 * @Version
 * @Date 2024/6/8 11:47 AM
 */

@ControllerAdvice
public class DecryptRequestBodyAdvice extends RequestBodyAdviceAdapter {
    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return false;
    }
    //
    // @Override
    // public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
    //     // 可以在这里添加逻辑判断是否需要解密，例如通过注解标记等
    //     return true; // 假设所有请求都需要解密
    // }
    //
    // @Override
    // public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
    //     // 实现解密逻辑，例如使用你自己的解密方法
    //     byte[] body = StreamUtils.copyToByteArray(inputMessage.getBody());
    //     byte[] decryptedBody = yourDecryptionMethod(body); // 请替换为你的解密方法
    //     return new DecryptedHttpInputMessage(inputMessage, decryptedBody);
    // }
    //
    // // 自定义HttpInputMessage，包装解密后的字节数组
    // private static class DecryptedHttpInputMessage implements HttpInputMessage {
    //
    //     private final HttpHeaders headers;
    //     private final InputStream body;
    //
    //     public DecryptedHttpInputMessage(HttpInputMessage inputMessage, byte[] body) {
    //         this.headers = inputMessage.getHeaders();
    //         this.body = new ByteArrayInputStream(body);
    //     }
    //
    //     @Override
    //     public InputStream getBody() throws IOException {
    //         return body;
    //     }
    //
    //     @Override
    //     public HttpHeaders getHeaders() {
    //         return headers;
    //     }
    //
    //     @Override
    //     public HttpStatus getStatusCode() throws IOException {
    //         return HttpStatus.OK; // 通常不被调用，但必须实现
    //     }
    //
    //     @Override
    //     public String getStatusText() throws IOException {
    //         return ""; // 同上
    //     }
    // }
}