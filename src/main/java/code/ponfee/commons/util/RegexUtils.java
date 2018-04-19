package code.ponfee.commons.util;

import static org.apache.oro.text.regex.Perl5Compiler.CASE_INSENSITIVE_MASK;
import static org.apache.oro.text.regex.Perl5Compiler.READ_ONLY_MASK;

import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * 正则工具类
 * http://blog.csdn.net/carechere/article/details/52315728
 * @author fupf
 */
public final class RegexUtils {
    private RegexUtils() {}

    private static final LoadingCache<String, org.apache.oro.text.regex.Pattern> PATTERNS =
        CacheBuilder.newBuilder().softValues().build(new CacheLoader<String, org.apache.oro.text.regex.Pattern>() {
            @Override
            public org.apache.oro.text.regex.Pattern load(String pattern) {
                try {
                    return new Perl5Compiler().compile(pattern, CASE_INSENSITIVE_MASK | READ_ONLY_MASK);
                } catch (MalformedPatternException e) {
                    throw new RuntimeException("Regex failed!", e);
                }
            }
        });

    /**
     * find the first match string from originalStr use regex
     * @param originalStr
     * @param regex
     * @return the first match string
     */
    public static String findFirst(String originalStr, String regex) {
        if (StringUtils.isBlank(originalStr) || StringUtils.isBlank(regex)) {
            return StringUtils.EMPTY;
        }

        PatternMatcher matcher = new Perl5Matcher();

        try {
            return matcher.contains(originalStr, PATTERNS.get(regex))
                   ? StringUtils.trimToEmpty(matcher.getMatch().group(0))
                   : StringUtils.EMPTY;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Pattern PATTERN_MOBILE = Pattern.compile(
        "^\\s*(((\\+)?86)|(\\((\\+)?86\\)))?1\\d{10}\\s*$"
    );

    /**
     * check is mobile phone
     * @param text
     * @return {@code true} is mobile phone
     */
    public static boolean isMobilePhone(String text) {
        return text != null && PATTERN_MOBILE.matcher(text).matches();
    }

    private static final Pattern PATTERN_EMAIL = Pattern.compile(
        "^\\w+((-\\w+)|(\\.\\w+))*\\@[A-Za-z0-9]+((\\.|-)[A-Za-z0-9]+)*\\.[A-Za-z0-9]+$"
    );

    /**
     * 校验是否邮箱地址
     * @param text
     * @return {@code true} is email address
     */
    public static boolean isEmail(String text) {
        return text != null && PATTERN_EMAIL.matcher(text).matches();
    }

    private static final Pattern PATTERN_IP = Pattern.compile(
        "((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))"
    );

    /**
     * 校验是否ip地址
     * @param text
     * @return {@code true} is ip address
     */
    public static boolean isIp(String text) {
        return text != null && PATTERN_IP.matcher(text).matches();
    }

    private static final Pattern PATTERN_USERNAME = Pattern.compile("^[0-9A-Za-z_\\-]{4,20}$");

    /**
     * 校验是否是有效的用户名
     * 数据库用户名字段最好不要区分大小写
     * @param text
     * @return {@code true} is valid user name
     */
    public static boolean isValidUserName(String text) {
        return text != null && PATTERN_USERNAME.matcher(text).matches();
    }

    private static final String SYMBOL = "@#!%&_\\.\\?\\-\\$\\^\\*";
    private static final Pattern PATTERN_PASSWORD = Pattern.compile(
        "^((?=.*\\d)(?=.*[A-Za-z])|(?=.*\\d)(?=.*[" + SYMBOL + "])|(?=.*[A-Za-z])(?=.*[" + SYMBOL + "]))[\\dA-Za-z" + SYMBOL + "]{8,20}$"
    );

    /**
     * 校验是否是有效的密码：
     *   > 8-20位
     *   > 必须包含字母、数字、符号中至少2种（可选的符号包括：@#!%&_.?-$^*）
     *   > 其它模式：^(?=.*\\d)(?=.*[A-Z])(?=.*[a-z])[\\dA-Za-z@#!%&_\\.\\?\\-\\$\\^\\*]{8,20}$
     *            ：^(?=.*\\d)(?=.*[A-Za-z])[\\dA-Za-z@#!%&_\\.\\?\\-\\$\\^\\*]{8,20}$
     *
     * isValidPassword("12131111") // false: 只有数字
     * isValidPassword("@#.@#.$^") // false: 只有字符
     * isValidPassword("aaaaaaaa") // false: 只有字母
     * isValidPassword("121311@1") // true: 数字字符
     * isValidPassword("121311A1") // true: 数字字母
     * isValidPassword("aaaaaa.a") // true: 字母字符
     * @param text
     * @return {@code true} is valid password
     */
    public static boolean isValidPassword(String text) {
        return text != null && PATTERN_PASSWORD.matcher(text).matches();
    }

    public static void main(String[] args) {
        System.out.println(isValidPassword("11ABac@#!%&_.?-$^*")); // true
        System.out.println(isValidPassword("12131111")); // false: 数字
        System.out.println(isValidPassword("1213Aa_")); // false: 7 length
        System.out.println(isValidPassword("1213Aa_11213Aa_112111")); // false: 21 length
    }
}
