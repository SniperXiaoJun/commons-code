package code.ponfee.commons.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.quartz.CronExpression;
import org.quartz.impl.calendar.BaseCalendar;
import org.quartz.impl.triggers.CronTriggerImpl;

import code.ponfee.commons.util.Dates;

/**
 * quartz工具类
 * @author fupf
 */
public class QuartzUtils {

    /**
     * 验证
     * @param cronExp
     * @return
     */
    public static boolean verifyCronExp(String cronExp) {
        List<Date> list = listNextExecTime(cronExp, 1);
        return list != null && list.size() == 1;
    }

    /**
     * 验证并获取后续的执行时间点列表
     * @param cronExp
     * @param count
     * @return
     */
    public static List<Date> listNextExecTime(String cronExp, int count) {
        return listNextExecTime(cronExp, count, new Date());
    }

    /**
     * 获取下个执行时间点
     * @param cronExp
     * @param begin
     * @return
     */
    public static Date getNextExecTime(String cronExp, Date begin) {
        List<Date> list = listNextExecTime(cronExp, 1, begin);
        if (list == null || list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    /**
     * 验证并获取后续的执行时间点列表
     * @param begin 开始时间
     * @param cronExp
     * @param count
     * @return
     */
    public static List<Date> listNextExecTime(String cronExp, int count, Date begin) {
        if (count < 1 || begin == null) {
            return null;
        }

        try {
            //CronScheduleBuilder.cronSchedule(cronExp);
            begin = Dates.plusSeconds(begin, 1);
            CronExpression exp = new CronExpression(cronExp);
            CronTriggerImpl trigger = new CronTriggerImpl();
            trigger.setCronExpression(exp);
            trigger.setStartTime(begin);
            Date date = trigger.computeFirstFireTime(new BaseCalendar());
            if (date == null || date.before(begin)) {
                return null;
            }

            List<Date> times = new ArrayList<>();
            times.add(date);
            for (int i = 1; i < count; i++) {
                date = exp.getNextValidTimeAfter(date);
                if (date == null) {
                    break;
                }
                times.add(date);
            }
            return times;
        } catch (ParseException e) {
            return null;
        }
    }

}
