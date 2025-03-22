package cn.org.alan.exam.common.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.spring.PropertyPreFilters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * AOP记录请求响应日志
 *
 * @Author Alan
 * @Version
 * @Date 2024/5/13 2:33 PM
 */
@Aspect
@Component
public class LogAsPect {

    // 定义日志记录器，用于记录日志信息
    private final static Logger LOG = LoggerFactory.getLogger(LogAsPect.class);

    /**
     * 定义切入点，匹配 cn.org.alan.exam.controller 包及其子包下所有以 Controller 结尾的类的所有公共方法
     * 即当这些方法被调用时，会触发对应的通知
     */
    @Pointcut("execution(public * cn.org.alan.exam.controller..*Controller.*(..))")
    public void controllerPointcut() {
    }

    /**
     * 在请求数据前打印参数
     *
     * @param joinPoint 连接点，包含了目标方法的相关信息
     * @throws Throwable 可能抛出的异常
     */
    @Before("controllerPointcut()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        // 获取当前请求的属性
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        // 获取 HttpServletRequest 对象，用于获取请求的相关信息
        HttpServletRequest request = attributes.getRequest();
        // 获取目标方法的签名，包含方法名、参数等信息
        Signature signature = joinPoint.getSignature();
        // 获取方法名
        String name = signature.getName();

        // 打印请求开始的日志信息
        LOG.info("------------- 开始 -------------");
        // 打印请求的 URL 和请求方法
        LOG.info("请求地址: {} {}", request.getRequestURL().toString(), request.getMethod());
        // 打印目标方法所在的类名和方法名
        LOG.info("类名方法: {}.{}", signature.getDeclaringTypeName(), name);
        // 打印请求的远程地址
        LOG.info("远程地址: {}", request.getRemoteAddr());

        // 获取目标方法的参数数组
        Object[] args = joinPoint.getArgs();
        // 创建一个新的数组，用于存储过滤后的参数
        Object[] arguments = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            // 过滤掉 ServletRequest、ServletResponse 和 MultipartFile 类型的参数
            if (args[i] instanceof ServletRequest
                    || args[i] instanceof ServletResponse
                    || args[i] instanceof MultipartFile) {
                continue;
            }
            arguments[i] = args[i];
        }

        // 定义需要排除的属性名，防止敏感信息（如密码、文件）被打印
        String[] excludeProperties = {"password", "file"};
        // 创建 FastJSON 的属性过滤器
        PropertyPreFilters filters = new PropertyPreFilters();
        // 添加排除过滤器
        PropertyPreFilters.MySimplePropertyPreFilter excludeFilter = filters.addFilter();
        // 设置需要排除的属性名
        excludeFilter.addExcludes(excludeProperties);
        // 打印过滤后的请求参数
        LOG.info("请求参数: {}", JSONObject.toJSONString(arguments, excludeFilter));
    }

    /**
     * 在请求后打印结果
     *
     * @param proceedingJoinPoint 可执行的连接点，用于调用目标方法
     * @return 目标方法的返回值
     * @throws Throwable 可能抛出的异常
     */
    @Around("controllerPointcut()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        // 调用目标方法并获取返回值
        Object result = proceedingJoinPoint.proceed();

        // 定义需要排除的属性名，防止敏感信息（如密码、文件）被打印
        String[] excludeProperties = {"password", "file"};
        // 创建 FastJSON 的属性过滤器
        PropertyPreFilters filters = new PropertyPreFilters();
        // 添加排除过滤器
        PropertyPreFilters.MySimplePropertyPreFilter excludeFilter = filters.addFilter();
        // 设置需要排除的属性名
        excludeFilter.addExcludes(excludeProperties);
        // 打印过滤后的返回结果
        LOG.info("返回结果: {}", JSONObject.toJSONString(result, excludeFilter));
        // 打印请求结束的日志信息，包括耗时
        LOG.info("------------- 结束 耗时：{} ms -------------", System.currentTimeMillis() - startTime);
        return result;
    }

}