package code.ponfee.commons.util;

import java.util.Date;

import org.apache.commons.lang3.time.FastDateFormat;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;

import com.google.common.base.Preconditions;

/**
 * 时间周期
 * @author Ponfee
 */
public enum DatePeriods {

    DAILY() {
        @Override
        public Duration next(DateTime original, Date target, int step, int next) {
            checkArguments(original, target, step);
            DateTime origin0 = original.withTimeAtStartOfDay();
            DateTime target0 = new DateTime(target).millisOfDay().withMaximumValue();
            Period period = new Period(origin0, target0, PeriodType.days());
            int interval = interval(period.getDays(), step, next);
            DateTime begin = origin0.plusDays(interval * step);
            return duration(begin, begin.plusDays(step));
        }
    },

    WEEKLY() {
        @Override
        public Duration next(DateTime original, Date target, int step, int next) {
            checkArguments(original, target, step);
            DateTime origin0 = original.withTimeAtStartOfDay();
            DateTime target0 = new DateTime(target).millisOfDay().withMaximumValue();
            Period period = new Period(origin0, target0, PeriodType.weeks());
            int interval = interval(period.getWeeks(), step, next);
            DateTime begin = origin0.plusWeeks(interval * step);
            return duration(begin, begin.plusWeeks(step));
        }
    },

    MONTHLY() {
        @Override
        public Duration next(DateTime original, Date target, int step, int next) {
            checkArguments(original, target, step);
            DateTime origin0 = original.withTimeAtStartOfDay();
            DateTime target0 = new DateTime(target).millisOfDay().withMaximumValue();

            Period period = new Period(origin0, target0, PeriodType.months());
            int interval = interval(period.getMonths(), step, next);
            DateTime begin = origin0.plusMonths(interval * step);
            return duration(begin, begin.plusMonths(step));
        }
    },

    QUARTERLY() {
        @Override
        public Duration next(DateTime original, Date target, int step, int next) {
            return MONTHLY.next(original, target, step * 3, next);
        }
    },

    HALF_YEARLY() {
        @Override
        public Duration next(DateTime original, Date target, int step, int next) {
            return MONTHLY.next(original, target, step * 6, next);
        }
    },

    YEARLY() {
        @Override
        public Duration next(DateTime original, Date target, int step, int next) {
            checkArguments(original, target, step);
            DateTime origin0 = original.withTimeAtStartOfDay();
            DateTime target0 = new DateTime(target).millisOfDay().withMaximumValue();

            Period period = new Period(origin0, target0, PeriodType.years());
            int interval = interval(period.getYears(), step, next);
            DateTime begin = origin0.plusYears(interval * step);
            return duration(begin, begin.plusYears(step));
        }
    };

    // 2018-01-01为星期一
    private static final DateTime ORIGINAL = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
                                                           .parseDateTime("2018-01-01 00:00:00");

    private static final FastDateFormat FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss SSS");

    /**
     * calculate the next period based original and reference target
     * @param original the period original
     * @param target   the target of next reference
     * @param step     the period step
     * @param next     the next of target period
     * @return Duration{start, end}
     */
    public abstract Duration next(DateTime original, Date target, int step, int next);

    public final Duration next(Date original, Date target, int step, int next) {
        return next(new DateTime(original), target, step, next);
    }

    public final Duration next(Date target, int step, int next) {
        return next(ORIGINAL, target, step, next);
    }

    public final Duration next(Date target, int next) {
        return next(ORIGINAL, target, 1, next);
    }

    // ---------------------------------------------------------------
    final void checkArguments(DateTime original, Date target, int step) {
        Preconditions.checkArgument(step > 0, "step must be positive number");
        Preconditions.checkArgument(!original.toDate().after(target));
    }

    final Duration duration(DateTime begin, DateTime end) {
        return new Duration(begin.withTimeAtStartOfDay().toDate(), 
                            end.minusMillis(1).toDate());
    }

    final int interval(int period, int step, int next) {
        return period / step + next;
    }

    public static class Duration {
        private final Date begin;
        private final Date end;

        private Duration(Date begin, Date end) {
            this.begin = begin;
            this.end = end;
        }

        public Date getBegin() {
            return begin;
        }

        public Date getEnd() {
            return end;
        }

        @Override
        public String toString() {
            return FORMAT.format(begin) + " ~ " + FORMAT.format(end);
        }
    }

}
