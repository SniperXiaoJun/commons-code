package code.ponfee.commons.util;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * 基于joda的日期工具类
 * @author fupf
 */
public class Dates {

    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 简单的日期格式校验(yyyy-MM-dd HH:mm:ss)
     * @param date 输入日期
     * @return 有效返回true, 反之false
     */
    public static boolean isValidDate(String date) {
        return isValidDate(date, DEFAULT_DATE_FORMAT);
    }

    /**
     * 简单的日期格式校验
     * @param date 输入日期，如(yyyy-MM-dd)
     * @param pattern 日期格式
     * @return 有效返回true, 反之false
     */
    public static boolean isValidDate(String date, String pattern) {
        if (StringUtils.isEmpty(date)) return false;

        try {
            toDate(date, pattern);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * 获取当前日期对象
     * @return 当前日期对象
     */
    public static Date now() {
        return new Date();
    }

    /**
     * 获取当前时间到1970.1.1的毫秒数
     * @return
     */
    public static long millis() {
        return millis(now());
    }

    public static long millis(Date date) {
        return date.getTime();
    }

    /**
     * 获取当前时间到1970.1.1的秒数
     * @return
     */
    public static long seconds() {
        return seconds(now());
    }

    public static long seconds(Date date) {
        return millis(date) / 1000;
    }

    /**
     * 获取当前日期字符串
     * @param format 日期格式
     * @return 当前日期字符串
     */
    public static String now(String format) {
        return format(now(), format);
    }

    /**
     * 转换日期字符串为日期对象(默认格式: yyyy-MM-dd HH:mm:ss)
     * @param dateStr 日期字符串
     * @return 日期对象
     */
    public static Date toDate(String dateStr) {
        return toDate(dateStr, DEFAULT_DATE_FORMAT);
    }

    /**
     * 转换日期即字符串为Date对象
     * @param dateStr 日期字符串
     * @param pattern 日期格式
     * @return 日期对象
     */
    public static Date toDate(String dateStr, String pattern) {
        return DateTimeFormat.forPattern(pattern).parseDateTime(dateStr).toDate();
    }

    /**
     * java（毫秒）时间戳
     * @param millis 毫秒
     * @return 日期
     */
    public static Date millis(long millis) {
        return new DateTime(millis).toDate();
    }

    /**
     * unix时间戳
     * @param seconds 秒
     * @return
     */
    public static Date seconds(long seconds) {
        return new DateTime(seconds * 1000).toDate();
    }

    /**
     * 格式化日期对象
     * @param date 日期对象
     * @param format 日期格式
     * @return 当前日期字符串
     */
    public static String format(Date date, String format) {
        return new DateTime(date).toString(format);
    }

    /**
     * 格式化日期对象，格式为yyyy-MM-dd HH:mm:ss
     * @param date 日期对象
     * @return 日期字符串
     */
    public static String format(Date date) {
        return new DateTime(date).toString(DEFAULT_DATE_FORMAT);
    }

    /**
     * 格式化日期对象，格式为yyyy-MM-dd HH:mm:ss
     * @param mills 毫秒
     * @return 日期字符串
     */
    public static String format(Long mills) {
        return new DateTime(mills).toString(DEFAULT_DATE_FORMAT);
    }

    /**
     * 格式化日期对象
     * @param mills 毫秒
     * @param pattern 格式
     * @return 日期字符串
     */
    public static String format(Long mills, String pattern) {
        return new DateTime(mills).toString(pattern);
    }

    /**
     * 计算两个日期的时间差（单位：秒）
     * @param start 开始时间
     * @param end 结束时间
     * @return 时间间隔
     */
    public static int clockdiff(Date start, Date end) {
        Objects.requireNonNull(start, "start date non null");
        Objects.requireNonNull(end, "end date non null");
        return (int) ((end.getTime() - start.getTime()) / 1000);
    }

    /**
     * 增加毫秒数
     * @param date 时间
     * @param numOfMillis 毫秒数
     * @return 时间
     */
    public static Date plusMillis(Date date, int numOfMillis) {
        return new DateTime(date).plusMillis(numOfMillis).toDate();
    }

    /**
     * 增加秒数
     * @param date 时间
     * @param numOfSeconds 秒数
     * @return 时间
     */
    public static Date plusSeconds(Date date, int numOfSeconds) {
        return new DateTime(date).plusSeconds(numOfSeconds).toDate();
    }

    /**
     * 增加分钟
     * @param date 时间
     * @param numOfMinutes 分钟数
     * @return 时间
     */
    public static Date plusMinutes(Date date, int numOfMinutes) {
        return new DateTime(date).plusMinutes(numOfMinutes).toDate();
    }

    /**
     * 增加小时
     * @param date 时间
     * @param numOfHours 小时数
     * @return 时间
     */
    public static Date plusHours(Date date, int numOfHours) {
        return new DateTime(date).plusHours(numOfHours).toDate();
    }

    /**
     * 增加天数
     * @param date 时间
     * @param numdays 天数
     * @return 时间
     */
    public static Date plusDays(Date date, int numdays) {
        return new DateTime(date).plusDays(numdays).toDate();
    }

    /**
     * 增加周
     * @param date 时间
     * @param numWeeks 周数
     * @return 时间
     */
    public static Date plusWeeks(Date date, int numWeeks) {
        return new DateTime(date).plusWeeks(numWeeks).toDate();
    }

    /**
     * 增加月份
     * @param date 时间
     * @param numMonths 月数
     * @return 时间
     */
    public static Date plusMonths(Date date, int numMonths) {
        return new DateTime(date).plusMonths(numMonths).toDate();
    }

    /**
     * 增加年
     * @param date 时间
     * @param numYears 年数
     * @return 时间
     */
    public static Date plusYears(Date date, int numYears) {
        return new DateTime(date).plusYears(numYears).toDate();
    }

    /**
     * 日期a是否大于日期b
     * @param source 待比较日期
     * @param target 目标日期
     * @return 大于返回true，反之false
     */
    public static Boolean isAfter(Date source, Date target) {
        return new DateTime(source).isAfter(target.getTime());
    }

    /**
     * 日期a是否大于当前日期
     * @param source 待比较日期
     * @return 大于返回true，反之false
     */
    public static Boolean isAfterNow(Date source) {
        return new DateTime(source).isAfterNow();
    }

    /**
     * 日期a是否小于日期b
     * @param source 待比较日期
     * @param target 目标日期
     * @return 小于返回true，反之false
     */
    public static Boolean isBefore(Date source, Date target) {
        return new DateTime(source).isBefore(target.getTime());
    }

    /**
     * 日期a是否大于当前日期
     * @param source 待比较日期
     * @return 小于返回true，反之false
     */
    public static Boolean isBeforeNow(Date source) {
        return new DateTime(source).isBeforeNow();
    }

    /**
     * 获取指定日期所在天的开始时间：yyyy-MM-dd 00:00:00
     * @param date 时间
     * @return 时间
     */
    public static Date startOfDay(Date date) {
        return new DateTime(date).withTimeAtStartOfDay().toDate();
    }

    /**
     * 获取指定日期所在天的结束时间：yyyy-MM-dd 23:59:59
     * @param date 时间
     * @return 时间
     */
    public static Date endOfDay(Date date) {
        return new DateTime(date).millisOfDay().withMaximumValue().toDate();
    }

    /**
     * 获取指定日期所在周的开始时间：yyyy-MM-周一 00:00:00
     * @param date 日期
     * @return 当前周第一天
     */
    public static Date startOfWeek(Date date) {
        return new DateTime(date).dayOfWeek().withMinimumValue().withTimeAtStartOfDay().toDate();
    }

    /**
     * 获取指定日期所在周的结束时间：yyyy-MM-周日 23:59:59
     * @param date 日期
     * @return 当前周最后一天
     */
    public static Date endOfWeek(Date date) {
        return new DateTime(date).dayOfWeek().withMaximumValue().millisOfDay().withMaximumValue().toDate();
    }

    /**
     * 获取指定日期所在月的开始时间：yyyy-MM-01 00:00:00
     * @param date 日期
     * @return 当前月的第一天
     */
    public static Date startOfMonth(Date date) {
        return new DateTime(date).dayOfMonth().withMinimumValue().withTimeAtStartOfDay().toDate();
    }

    /**
     * 获取指定日期所在月的结束时间：yyyy-MM-月未 23:59:59
     * @param date 日期
     * @return 当前月的最后一天
     */
    public static Date endOfMonth(Date date) {
        return new DateTime(date).dayOfMonth().withMaximumValue().millisOfDay().withMaximumValue().toDate();
    }

    /**
     * 获取指定日期所在月的开始时间：yyyy-01-01 00:00:00
     * @param date 日期
     * @return 当前年的第一天
     */
    public static Date startOfYear(Date date) {
        return new DateTime(date).dayOfYear().withMinimumValue().withTimeAtStartOfDay().toDate();
    }

    /**
     * 获取指定日期所在月的结束时间：yyyy-12-31 23:59:59
     * @param date 日期
     * @return 当前年的最后一天
     */
    public static Date endOfYear(Date date) {
        return new DateTime(date).dayOfYear().withMaximumValue().millisOfDay().withMaximumValue().toDate();
    }

    /**
     * 获取当前时间所在周的周n
     * @param day 1:星期一，2:星期二，...
     * @return 本周周几的日期对象
     */
    public static Date currentDayOfWeek(int day) {
        return dayOfWeek(now(), day);
    }

    /**
     * 获取指定时间所在周的周n，1<=day<=7
     * @param date
     * @param day
     * @return
     */
    public static Date dayOfWeek(Date date, int day) {
        return new DateTime(startOfDay(date)).withDayOfWeek(day).toDate();
    }

    /**
     * 获取当前时间所在月的n号，1<=day<=31
     * @param day
     * @return
     */
    public static Date currentDayOfMonth(int day) {
        return dayOfMonth(now(), day);
    }

    /**
     * 获取指定时间所在月的n号，1<=day<=31
     * @param date
     * @param day
     * @return
     */
    public static Date dayOfMonth(Date date, int day) {
        return new DateTime(startOfDay(date)).withDayOfMonth(day).toDate();
    }

    /**
     * 获取当前时间所在年的n天，1<=day<=366
     * @param day
     * @return
     */
    public static Date currentDayOfYear(int day) {
        return dayOfYear(now(), day);
    }

    /**
     * 获取指定时间所在年的n天，1<=day<=366
     * @param date
     * @param day
     * @return
     */
    public static Date dayOfYear(Date date, int day) {
        return new DateTime(startOfDay(date)).withDayOfYear(day).toDate();
    }

    /**
     * 日期随机
     * @param begin  开发日期
     * @param end    结束日期
     * @return
     */
    public static Date random(Date begin, Date end) {
        int seconds = ThreadLocalRandom.current().nextInt(clockdiff(begin, end));
        return Dates.plusSeconds(begin, seconds);
    }

    public static Date random(Date begin) {
        return random(begin, now());
    }

    public static Date random() {
        return random(millis(0), now());
    }

    public static void main(String[] args) {
        System.out.println(isValidDate("2017-02-29", "yyyy-MM-dd"));
        System.out.println(format(startOfDay(new Date()), "yyyy-MM-dd HH:mm:ss SSS"));
        System.out.println(format(endOfDay(new Date()), "yyyy-MM-dd HH:mm:ss SSS"));
        System.out.println(format(startOfWeek(new Date()), "yyyy-MM-dd HH:mm:ss SSS"));
        System.out.println(format(endOfWeek(new Date()), "yyyy-MM-dd HH:mm:ss SSS"));
        System.out.println(format(startOfMonth(new Date()), "yyyy-MM-dd HH:mm:ss SSS"));
        System.out.println(format(endOfMonth(new Date()), "yyyy-MM-dd HH:mm:ss SSS"));
        System.out.println(format(startOfYear(new Date()), "yyyy-MM-dd HH:mm:ss SSS"));
        System.out.println(format(endOfYear(new Date()), "yyyy-MM-dd HH:mm:ss SSS"));

        System.out.println("============================================");
        System.out.println(format(currentDayOfWeek(1), "yyyy-MM-dd HH:mm:ss SSS"));
        System.out.println(format(currentDayOfMonth(15), "yyyy-MM-dd HH:mm:ss SSS"));
        System.out.println(format(currentDayOfYear(155), "yyyy-MM-dd HH:mm:ss SSS"));

        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        System.out.println(ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        System.out.println(Dates.format(now(), "yyyy-MM-dd'T'HH:mm:ss.SSS"));
    }
}
