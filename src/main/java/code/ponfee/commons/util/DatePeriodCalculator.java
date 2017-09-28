package code.ponfee.commons.util;

import java.util.Calendar;
import java.util.Date;

/**
 * 周期计算
 * @author Ponfee
 */
public class DatePeriodCalculator {

    private static final Date STARTING_DATE = Dates.toDate("2007-01-01 00:00:00", "yyyy-MM-dd HH:mm:ss");

    private final Date starting; // 最开始的周期（起点）时间
    private final Date target; // 待计算时间
    private final Period period; // 周期类型

    public DatePeriodCalculator(Date target, Period period) {
        this(STARTING_DATE, target, period);
    }

    public DatePeriodCalculator(Date starting, Date target, Period period) {
        this.starting = starting;
        this.target = target;
        this.period = period;
    }

    /**
     * @param quantity 周期数量
     * @param next     目标周期的下next个周期
     * @return
     */
    public Date[] calculate(int quantity, int next) {
        if (quantity < 1) throw new IllegalArgumentException("quantity must be positive number");
        if (starting.after(target)) throw new IllegalArgumentException("starting canot after target date");

        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(starting);
        c2.setTime(target);
        Date startDate = null;
        int cycleNum, year;
        Calendar tmp;
        float days;
        switch (period) {
            case WEEKLY:
                quantity *= 7;
            case DAILY:
                days = c2.get(Calendar.DAY_OF_YEAR) - c1.get(Calendar.DAY_OF_YEAR); // 间隔天数
                year = c2.get(Calendar.YEAR);
                tmp = (Calendar) c1.clone();
                while (tmp.get(Calendar.YEAR) != year) {
                    days += tmp.getActualMaximum(Calendar.DAY_OF_YEAR);// 得到当年的实际天数
                    tmp.add(Calendar.YEAR, 1);
                }
                cycleNum = (int) Math.floor(days / quantity) + next; // 上一个周期
                c1.add(Calendar.DAY_OF_YEAR, cycleNum * quantity);
                startDate = c1.getTime();
                c1.add(Calendar.DAY_OF_YEAR, quantity);
                break;
            case QUARTERLY: // 季度
            case HALF_YEARLY: // 半年度
            case YEARLY: // 年度
            case MONTHLY: // 月
                switch (period) {
                    case QUARTERLY: // 季度
                        quantity *= 3;
                        break;
                    case HALF_YEARLY: // 半年度
                        quantity *= 6;
                        break;
                    case YEARLY:
                        quantity *= 12; // 年度
                        break;
                    default:
                        throw new IllegalArgumentException("invalid period type");
                }
                // 间隔月数
                int intervalMonth = (c2.get(Calendar.YEAR) - c1.get(Calendar.YEAR)) * 12 + c2.get(Calendar.MONTH) - c1.get(Calendar.MONTH);
                cycleNum = (int) Math.floor(intervalMonth / quantity);
                tmp = (Calendar) c1.clone();
                // 跨月问题，当前时间仍属于该周期内，则应减一个周期数，如：(2012-01-15 ~ 2012-02-14，当前时间为2012-02-14，则当前时间属于该周期，而不是下一周期)
                tmp.add(Calendar.MONTH, cycleNum * quantity);
                if (tmp.after(c2)) cycleNum -= 1;
                cycleNum += next; // 上一个周期
                c1.add(Calendar.MONTH, cycleNum * quantity);
                startDate = c1.getTime(); // 本周期开始时间
                c1.add(Calendar.MONTH, quantity); // 本周期结束时间
                break;
            default:
                throw new IllegalArgumentException("invalid period type");
        }
        c1.add(Calendar.MILLISECOND, -1);
        return new Date[] { startDate, c1.getTime() };
    }

    public Date[] calculate(int next) {
        return calculate(1, next);
    }

    public static enum Period {
        DAILY("每日"), // 
        WEEKLY("每周"), // 
        MONTHLY("每月"), // 
        QUARTERLY("每季度"), // 
        HALF_YEARLY("每半年"), // 
        YEARLY("每年");
        private final String desc;

        Period(String desc) {
            this.desc = desc;
        }

        public String desc() {
            return this.desc;
        }
    }

    public static void main(String[] args) {
        Date[] dates = new DatePeriodCalculator(new Date(), Period.WEEKLY).calculate(-1);
        System.out.println(Dates.format(dates[0], "yyyy-MM-dd HH:mm:ss SSS") + "  ~  "
            + Dates.format(dates[1], "yyyy-MM-dd HH:mm:ss SSS"));
    }
}
