package code.ponfee.commons.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

/**
 * 数字精度工具类
 * @author fupf
 */
public class Numbers {
    public static final Integer INTEGER_ZERO = Integer.valueOf(0);
    private static final String[] CN_UPPER_NUMBER = { "零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖" };
    private static final String[] CN_UPPER_MONETRAY_UNIT = { "分", "角", "元", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿", "拾", "佰", "仟", "兆", "拾", "佰", "仟" };

    /**
     * 数字精度化
     * @param value
     * @param scale
     * @return
     */
    public static double scale(Object value, int scale) {
        if (value == null) return 0;
        if (scale < 0) Double.parseDouble(value.toString());

        BigDecimal b = new BigDecimal(value.toString());
        return b.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 向下转单位
     * @param value
     * @param scale
     * @return
     */
    public static double lower(double value, int scale) {
        BigDecimal b = new BigDecimal(value / Math.pow(10, scale));
        return b.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
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

    public static String percent(double numerator, double denominator, int scale) {
        if (denominator == 0) return "--";
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
                return formatter.format(Double.valueOf(str.substring(0, str.length() - 1))) + "%";
            } else {
                return formatter.format(Double.valueOf(str));
            }
        } else {
            return formatter.format(obj);
        }
    }

    public static double add(Double num1, Double num2) {
        num1 = num1 == null ? 0 : num1;
        num2 = num2 == null ? 0 : num2;
        return num1 + num2;
    }

    public static int range(Integer value, int min, int max) {
        if (value == null || value < min) return min;
        else if (value > max) return max;
        else return value;
    }

    public static int toInt(Object obj) {
        if (obj == null) return 0;
        return Integer.parseInt(obj.toString());
    }

    /**
     * 均分
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
     * 金额汉化
     * @param amount
     * @return
     */
    public static String amountChinese(BigDecimal amount) {
        StringBuilder builder = new StringBuilder();
        // -1, 0, or 1 as the value of this BigDecimal is negative, zero, or
        int signum = amount.signum(); // positive
        if (signum == 0) return "零元整"; // 零元整的情况
        //这里会进行金额的四舍五入
        long number = amount.movePointRight(2).setScale(0, 4).abs().longValue();
        // 得到小数点后两位值
        long scale = number % 100;
        int numUnit = 0, numIndex = 0;
        boolean getZero = false;
        // 判断最后两位数，一共有四中情况：00 = 0, 01 = 1, 10, 11
        if (scale <= 0) {
            numIndex = 2;
            number = number / 100;
            getZero = true;
        }
        if (scale > 0 && scale % 10 <= 0) {
            numIndex = 1;
            number = number / 10;
            getZero = true;
        }
        int zeroSize = 0;
        while (number > 0) {
            // 每次获取到最后一个数
            numUnit = (int) (number % 10);
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
                if (!(getZero)) {
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
            // 让number每次都去掉最后一个数
            number = number / 10;
            ++numIndex;
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
        System.out.println(((double) 15 / 2));
        System.out.println(Math.pow(10, 2));
        System.out.println(lower(441656, 2));
        System.out.println(percent(0.00241, 1));
        System.out.println(add(0.00241, 1d));

        System.out.println(ObjectUtils.toString(sharding(10, 20)));

        double money = 2020004.01;
        String s = amountChinese(new BigDecimal(money));
        System.out.println("[" + money + "]   ->   [" + s.toString() + "]");
    }
}
