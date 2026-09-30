package cn.org.alan.exam.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间工具类
 *
 * @Author WeiJin
 * @Version 1.0
 * @Date 2024/3/28 13:39
 */
public class DateTimeUtil {

    private DateTimeUtil() {
    }
    private static String dataFormat = "yyyy-MM-dd";
    private static String format = "yyyy-MM-dd HH:mm:ss";

    /**
     * 获取当前时间 使用yyyy-MM-dd HH:mm:ss格式
     *
     * @return 返回当前时间
     */
    public static LocalDateTime getDateTime() {
        return LocalDateTime.parse(datetimeToStr(LocalDateTime.now()), DateTimeFormatter.ofPattern(format));
    }
    public static LocalDate getDate() {
        return LocalDate.parse(dateToStr(LocalDate.now()), DateTimeFormatter.ofPattern(dataFormat));
    }

    /**
     * 把字符串时间日期转为LocalDateTime格式
     *
     * @param dateTime 需要转换的字符串
     * @return 返回LocalDateTime格式
     */
    public static String datetimeToStr(LocalDateTime dateTime) {
        return DateTimeFormatter.ofPattern(format).format(dateTime);

    }
    public static String dateToStr(LocalDate date) {
        return DateTimeFormatter.ofPattern(dataFormat).format(date);

    }

}
