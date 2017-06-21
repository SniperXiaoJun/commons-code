package code.ponfee.commons.util;

import java.util.Calendar;
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
    private static final Date ORIGIN_DATE = toDate("2007-01-01 00:00:00", DEFAULT_DATE_FORMAT);

    /**
     * 简单的日期格式校验(yyyy-MM-dd HH:mm:ss)
     * @param date 输入日期
     * @return 有效返回true, 反之false
     */
    public static Boolean isValidDate(String date) {
        return isValidDate(date, DEFAULT_DATE_FORMAT);
    }

    /**
     * 简单的日期格式校验
     * @param date 输入日期，如(yyyy-MM-dd)
     * @param pattern 日期格式
     * @return 有效返回true, 反之false
     */
    public static Boolean isValidDate(String date, String pattern) {
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
        return now().getTime();
    }

    /**
     * 获取当前时间到1970.1.1的秒数
     * @return
     */
    public static long seconds() {
        return millis() / 1000;
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
     * 生成时间
     * @param millis 毫秒
     * @return 日期
     */
    public static Date toDate(long millis) {
        return new DateTime(millis).toDate();
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
     * @param a 日期a
     * @param b 日期b
     * @return 大于返回true，反之false
     */
    public static Boolean isAfter(Date a, Date b) {
        return new DateTime(a).isAfter(b.getTime());
    }

    /**
     * 日期a是否大于当前日期
     * @param a 日期a
     * @return 大于返回true，反之false
     */
    public static Boolean isAfterNow(Date a) {
        return new DateTime(a).isAfterNow();
    }

    /**
     * 日期a是否小于日期b
     * @param src 日期a
     * @param target 日期b
     * @return 小于返回true，反之false
     */
    public static Boolean isBefore(Date src, Date target) {
        return new DateTime(src).isBefore(target.getTime());
    }

    /**
     * 日期a是否大于当前日期
     * @param a 日期a
     * @return 小于返回true，反之false
     */
    public static Boolean isBefore(Date a) {
        return new DateTime(a).isBeforeNow();
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
    public static Date dayOfWeek(Integer day) {
        return dayOfWeek(now(), day);
    }

    /**
     * 获取指定时间所在周的周n，1<=day<=7
     * @param date
     * @param day
     * @return
     */
    public static Date dayOfWeek(Date date, Integer day) {
        return new DateTime(startOfDay(date)).withDayOfWeek(day).toDate();
    }

    /**
     * 获取当前时间所在月的n号，1<=day<=31
     * @param day
     * @return
     */
    public static Date dayOfMonth(Integer day) {
        return dayOfMonth(now(), day);
    }

    /**
     * 获取指定时间所在月的n号，1<=day<=31
     * @param date
     * @param day
     * @return
     */
    public static Date dayOfMonth(Date date, Integer day) {
        return new DateTime(startOfDay(date)).withDayOfMonth(day).toDate();
    }

    /**
     * 获取当前时间所在年的n天，1<=day<=366
     * @param day
     * @return
     */
    public static Date dayOfYear(Integer day) {
        return dayOfYear(now(), day);
    }

    /**
     * 获取指定时间所在年的n天，1<=day<=366
     * @param date
     * @param day
     * @return
     */
    public static Date dayOfYear(Date date, Integer day) {
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
        return random(toDate(0), now());
    }

    /**
     * @param origin 起源（起始）时间
     * @param type 类型
     * @param reference 目标时间
     * @param interval 周期数
     * @param next 目标周期的下next周期
     * @return
     */
    public static Date[] calculateCycle(Date origin, String type, Date reference, int interval, int next) {
        if (interval < 1) {
            throw new IllegalArgumentException("interval mus be positive number");
        }
        if (origin.after(reference)) {
            throw new IllegalArgumentException("end date must be after begin date");
        }

        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(origin);
        c2.setTime(reference);
        Date startDate = null;
        int cycleNum, year;
        Calendar tmp;
        float days;
        switch (type) {
            case "weekly":
                interval *= 7;
            case "daily":
                days = c2.get(Calendar.DAY_OF_YEAR) - c1.get(Calendar.DAY_OF_YEAR); // 间隔天数
                year = c2.get(Calendar.YEAR);
                tmp = (Calendar) c1.clone();
                while (tmp.get(Calendar.YEAR) != year) {
                    days += tmp.getActualMaximum(Calendar.DAY_OF_YEAR);// 得到当年的实际天数
                    tmp.add(Calendar.YEAR, 1);
                }
                cycleNum = (int) Math.floor(days / interval) + next; // 上一个周期
                c1.add(Calendar.DAY_OF_YEAR, cycleNum * interval);
                startDate = c1.getTime();
                c1.add(Calendar.DAY_OF_YEAR, interval);
                break;
            case "quarterly": // 季度
            case "half_yearly": // 半年度
            case "yearly": // 年度
            case "monthly": // 月
            case "month_once":
                switch (type) {
                    case "quarterly": // 季度
                        interval *= 3;
                        break;
                    case "half_yearly": // 半年度
                        interval *= 6;
                        break;
                    case "yearly":
                        interval *= 12; // 年度
                        break;
                    default:
                        throw new IllegalArgumentException("invalid cycle type");
                }
                int intervalMonth = (c2.get(Calendar.YEAR) - c1.get(Calendar.YEAR)) * 12 + c2.get(Calendar.MONTH) - c1.get(Calendar.MONTH); // 间隔月数
                cycleNum = (int) Math.floor(intervalMonth / interval);
                tmp = (Calendar) c1.clone();
                tmp.add(Calendar.MONTH, cycleNum * interval); // 跨月问题，当前时间仍属于该周期内，则应减一个周期数，如：(2012-01-15 ~ 2012-02-14，当前时间为2012-02-14，则当前时间属于该周期，而不是下一周期)
                if (tmp.after(c2)) cycleNum -= 1;
                cycleNum += next; // 上一个周期
                c1.add(Calendar.MONTH, cycleNum * interval);
                startDate = c1.getTime(); // 本周期开始时间
                c1.add(Calendar.MONTH, interval); // 本周期结束时间
                break;
            default:
                throw new IllegalArgumentException("invalid cycle type");
        }
        c1.add(Calendar.MILLISECOND, -1);
        return new Date[] { startDate, c1.getTime() };
    }

    public static Date[] calculateCycle(String type, Date reference, int next) {
        return calculateCycle(ORIGIN_DATE, type, reference, 1, next);
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
        System.out.println(format(dayOfWeek(1), "yyyy-MM-dd HH:mm:ss SSS"));
        System.out.println(format(dayOfMonth(15), "yyyy-MM-dd HH:mm:ss SSS"));
        System.out.println(format(dayOfYear(155), "yyyy-MM-dd HH:mm:ss SSS"));

        System.out.println("============================================");
        Date[] dates = calculateCycle("weekly", new Date(), -1);
        System.out.println(format(dates[0], "yyyy-MM-dd HH:mm:ss SSS") + "  ~  " + format(dates[1], "yyyy-MM-dd HH:mm:ss SSS"));
        System.out.println(format(toDate(0), "yyyy-MM-dd HH:mm:ss SSS"));
    }
}
