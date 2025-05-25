package cn.org.alan.exam.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class IPUtils {

    private static final Logger log = LogManager.getLogger(IPUtils.class);

    private static final String DB_PATH = "src/main/resources/ipdata/ip2region.xdb";

    private static final ThreadLocal<Searcher> searcherThreadLocal = ThreadLocal.withInitial(() -> {
        try {
            return Searcher.newWithFileOnly(DB_PATH);
        } catch (Exception e) {
            log.error("初始化 IP 归属地查询失败: {}", e.getMessage());
            return null;
        }
    });

    /**
     * 获取ip归属地
     * @param request
     * @return
     */

    public static String getIPRegion(HttpServletRequest request) {
        String ip = getIPAddress(request);
        Searcher searcher = searcherThreadLocal.get();
        if (searcher == null) {
            log.error("IP 归属地查询失败，返回空");
            return null;
        }
        try {
            long startTime = System.nanoTime();
            String region = searcher.search(ip);
            long cost = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startTime);
            log.info("IP: {}, Region: {}, IO Count: {}, Took: {} μs", ip, region, searcher.getIOCount(), cost);
            return region;
        } catch (Exception e) {
            log.error("IP: {} 获取 IP 归属地错误，错误原因: {}", ip, e.getMessage());
            return null;
        } finally {
            closeSearcher();
        }
    }

    /**
     * 获取ip地址
     * @param request
     * @return
     */
    private static String getIPAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    public static void closeSearcher() {
        try {
            Searcher searcher = searcherThreadLocal.get();
            if (Objects.nonNull(searcher)) {
                searcher.close();
                searcherThreadLocal.remove();
            }
        } catch (Exception e) {
            log.error("关闭异常", e);
        }
    }
}