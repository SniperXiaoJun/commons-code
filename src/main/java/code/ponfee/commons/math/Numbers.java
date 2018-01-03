package code.ponfee.commons.math;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import code.ponfee.commons.util.ObjectUtils;

/**
 * 数字工具类
 * @author Ponfee
 */
public final class Numbers {

    public static final Integer INTEGER_ZERO = Integer.valueOf(0);

    // -----------------------------------to primary number------------------------------
    public static int toInt(Object obj) {
        return toInt(obj, 0);
    }

    public static int toInt(Object obj, int defaultVal) {
        return ((Double) toDouble(obj, defaultVal)).intValue();
    }

    public static long toLong(Object obj) {
        return toLong(obj, 0L);
    }

    public static long toLong(Object obj, long defaultVal) {
        return ((Double) toDouble(obj, defaultVal)).longValue();
    }

    public static float toFloat(Object obj) {
        return toFloat(obj, 0.0F);
    }

    public static float toFloat(Object obj, float defaultVal) {
        return ((Double) toDouble(obj, defaultVal)).floatValue();
    }

    public static double toDouble(Object obj) {
        return toDouble(obj, 0.0D);
    }

    public static double toDouble(Object obj, double defaultVal) {
        if (obj == null) {
            return defaultVal;
        }

        if (Number.class.isInstance(obj)) {
            return ((Number) obj).doubleValue();
        }

        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException ignored) {
            return defaultVal;
        }
    }

    // -----------------------------------to wrapper number------------------------------
    public static Double toWrapDouble(Number value) {
        return value == null ? null : value.doubleValue();
    }

    public static Integer toWrapInt(Object obj) {
        return toWrapInt(obj, null);
    }

    public static Integer toWrapInt(Object obj, Integer defaultVal) {
        Double value = toWrapDouble(obj, toWrapDouble(defaultVal));
        return value == null ? null : value.intValue();
    }

    public static Long toWrapLong(Object obj) {
        return toWrapLong(obj, null);
    }

    public static Long toWrapLong(Object obj, Long defaultVal) {
        Double value = toWrapDouble(obj, toWrapDouble(defaultVal));
        return value == null ? null : value.longValue();
    }

    public static Float toWrapFloat(Object obj) {
        return toWrapFloat(obj, null);
    }

    public static Float toWrapFloat(Object obj, Float defaultVal) {
        Double value = toWrapDouble(obj, toWrapDouble(defaultVal));
        return value == null ? null : value.floatValue();
    }

    public static Double toWrapDouble(Object obj) {
        return toWrapDouble(obj, null);
    }

    public static Double toWrapDouble(Object obj, Double defaultVal) {
        if (obj == null) {
            return defaultVal;
        }

        if (Number.class.isInstance(obj)) {
            return ((Number) obj).doubleValue();
        }

        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException ignored) {
            return defaultVal;
        }
    }

    /**
     * 数字精度化
     * @param value
     * @param scale
     * @return
     */
    public static double scale(Object value, int scale) {
        Double val = toDouble(value);

        if (scale < 0) {
            return val;
        }

        return new BigDecimal(val).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 向下转单位
     * @param value
     * @param scale
     * @return
     */
    public static double lower(double value, int scale) {
        return new BigDecimal(value / Math.pow(10, scale))
               .setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 向上转单位
     * @param value
     * @param pow
     * @return
     */
    public static double upper(double value, int pow) {
        return new BigDecimal(value * Math.pow(10, pow)).doubleValue();
    }

    /**
     * 百分比
     * @param numerator
     * @param denominator
     * @param scale
     * @return
     */
    public static String percent(double numerator, double denominator, int scale) {
        if (denominator == 0) {
            return "--";
        }

        return percent(numerator / denominator, scale);
    }

    /**
     * 百分比
     * @param value
     * @param scale
     * @return
     */
    public static String percent(double value, int scale) {
        String format = "#,##0";
        if (scale > 0) {
            format += "." + StringUtils.leftPad("", scale, '0');
        }
        return new DecimalFormat(format + "%").format(value);
    }

    /**
     * 数字格式化
     * @param obj
     * @return
     */
    public static String format(Object obj) {
        return format(obj, "###,###.###");
    }

    /**
     * 数字格式化
     * @param obj
     * @param format
     * @return
     */
    public static String format(Object obj, String format) {
        NumberFormat formatter = new DecimalFormat(format);
        if (obj instanceof CharSequence) {
            String str = obj.toString().replaceAll(",", "");
            if (str.endsWith("%")) {
                return formatter.format(Double.parseDouble(str.substring(0, str.length() - 1))) + "%";
            } else {
                return formatter.format(Double.parseDouble(str));
            }
        } else {
            return formatter.format(obj);
        }
    }

    /**
     * 两数相加
     * @param num1
     * @param num2
     * @return
     */
    public static double add(Double num1, Double num2) {
        return ObjectUtils.ifNull(num1, 0D)
             + ObjectUtils.ifNull(num2, 0D);
    }

    /**
     * 区间取值
     * @param value
     * @param min
     * @param max
     * @return
     */
    public static int bounds(Integer value, int min, int max) {
        if (value == null || value < min) {
            return min;
        } else if (value > max) {
            return max;
        } else {
            return value;
        }
    }

    /**
     * 分片
     * @param quantity
     * @param segment
     * @return
     */
    public static int[] sharding(int quantity, int segment) {
        int[] array = new int[segment];
        int remainder = quantity % segment;
        Arrays.fill(array, 0, remainder, quantity / segment + 1);
        Arrays.fill(array, remainder, segment, quantity / segment);
        return array;
    }

    /**
     * 数字比较
     * @param a
     * @param b
     * @return
     */
    public static boolean equals(Long a, Long b) {
        return (a == b) || (a != null && a.equals(b));
    }

    /**
     * 数字比较
     * @param a
     * @param b
     * @return
     */
    public static boolean equals(Long a, Integer b) {
        return a != null && b != null && a.intValue() == b.intValue();
    }

    /**
     * 数字比较
     * @param a
     * @param b
     * @return
     */
    public static boolean equals(Integer a, Integer b) {
        return (a == b) || (a != null && a.equals(b));
    }

    private static final String[] CN_UPPER_NUMBER = { "零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖" };
    private static final String[] CN_UPPER_MONETRAY_UNIT = { "分", "角", "元", "拾", "佰", "仟", "万", "拾", "佰",
                                                             "仟", "亿", "拾", "佰", "仟", "兆", "拾", "佰", "仟" };
    /**
     * 金额汉化
     * @param amount
     * @return
     */
    public static String chinesize(BigDecimal amount) {
        int signum = amount.signum(); // 正负数：0,1,-1
        if (signum == 0) {
            return "零元整"; // 零元整的情况
        }

        // 这里会进行金额的四舍五入
        long number = amount.movePointRight(2).setScale(0, 4).abs().longValue();
        long scale = number % 100; // 得到小数点后两位值
        int numIndex = 0;
        boolean getZero = false;
        if (scale <= 0) { // 判断最后两位数，一共有四中情况：00 = 0, 01 = 1, 10, 11
            numIndex = 2;
            number = number / 100;
            getZero = true;
        }
        if (scale > 0 && scale % 10 <= 0) {
            numIndex = 1;
            number = number / 10;
            getZero = true;
        }
        StringBuilder builder = new StringBuilder();
        for (int zeroSize = 0, numUnit = 0; number > 0; number = number / 10, ++numIndex) {
            numUnit = (int) (number % 10); // 每次获取到最后一个数
            if (numUnit > 0) {
                if ((numIndex == 9) && (zeroSize >= 3)) {
                    builder.insert(0, CN_UPPER_MONETRAY_UNIT[6]);
                }
                if ((numIndex == 13) && (zeroSize >= 3)) {
                    builder.insert(0, CN_UPPER_MONETRAY_UNIT[10]);
                }
                builder.insert(0, CN_UPPER_MONETRAY_UNIT[numIndex]);
                builder.insert(0, CN_UPPER_NUMBER[numUnit]);
                getZero = false;
                zeroSize = 0;
            } else {
                ++zeroSize;
                if (!getZero) {
                    builder.insert(0, CN_UPPER_NUMBER[numUnit]);
                }
                if (numIndex == 2) {
                    if (number > 0) {
                        builder.insert(0, CN_UPPER_MONETRAY_UNIT[numIndex]);
                    }
                } else if (((numIndex - 2) % 4 == 0) && (number % 1000 > 0)) {
                    builder.insert(0, CN_UPPER_MONETRAY_UNIT[numIndex]);
                }
                getZero = true;
            }
        }

        // 如果signum == -1，则说明输入的数字为负数，就在最前面追加特殊字符：负
        if (signum == -1) {
            builder.insert(0, "负");
        }
        // 输入的数字小数点后两位为"00"的情况，则要在最后追加特殊字符：整
        if (!(scale > 0)) {
            builder.append("整");
        }
        return builder.toString();
    }

    public static void main(String[] args) {
        System.out.println(lower(441656, 2));
        System.out.println(percent(0.00241, 1));

        System.out.println(ObjectUtils.toString(sharding(10, 20)));

        double money = 2020004.01;
        String s = chinesize(new BigDecimal(money));
        System.out.println("[" + money + "]   ->   [" + s.toString() + "]");
    }
}
