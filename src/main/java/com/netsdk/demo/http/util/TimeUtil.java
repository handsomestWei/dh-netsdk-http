package com.netsdk.demo.http.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeUtil {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 如果 timeStr 为空，返回当天 00:00:00，否则返回原值
     */
    public static String getDefaultStartIfBlank(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            LocalDate today = LocalDate.now();
            return today.atStartOfDay().format(FORMATTER);
        }
        return timeStr;
    }

    /**
     * 如果 timeStr 为空，返回当天 23:59:59，否则返回原值
     */
    public static String getDefaultEndIfBlank(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            LocalDate today = LocalDate.now();
            return today.atTime(LocalTime.MAX).format(FORMATTER);
        }
        return timeStr;
    }
} 