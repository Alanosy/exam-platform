package cn.org.alan.exam.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
/**
 * @Author Alan
 * @Version
 * @Date 2024/5/28 11:28 PM
 */ class DateTimeUtilTest {

    @Test
    void getDate() {
        LocalDate date = DateTimeUtil.getDate();
        System.out.println(date);
    }

    @Test
    void dateToStr() {
        System.out.println(DateTimeUtil.dateToStr(DateTimeUtil.getDate()));
    }
}