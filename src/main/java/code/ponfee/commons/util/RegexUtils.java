package code.ponfee.commons.util;

import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * 正则工具类
 * @author fupf
 */
public final class RegexUtils {
    private static final Pattern PATTERN_MOBILE = Pattern.compile("^\\s*(((\\+)?86)|(\\((\\+)?86\\)))?1\\d{10}\\s*$");
    private static final Pattern PATTERN_EMAIL = Pattern.compile("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
    private static final Pattern PATTERN_IP = Pattern.compile("((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))");

    private RegexUtils() {}

    private static final LoadingCache<String, org.apache.oro.text.regex.Pattern> PATTERNS =
        CacheBuilder.newBuilder().softValues().build(new CacheLoader<String, org.apache.oro.text.regex.Pattern>() {
            @Override
            public org.apache.oro.text.regex.Pattern load(String pattern) throws Exception {
                try {
                    PatternCompiler pc = new Perl5Compiler();
                    return pc.compile(pattern, Perl5Compiler.CASE_INSENSITIVE_MASK | Perl5Compiler.READ_ONLY_MASK);
                } catch (MalformedPatternException e) {
                    throw new RuntimeException("Regex failed!", e);
                }
            }
        });

    public static String findFirst(String originalStr, String regex) {
        if (StringUtils.isBlank(originalStr) || StringUtils.isBlank(regex)) {
            return StringUtils.EMPTY;
        }

        PatternMatcher matcher = new Perl5Matcher();
        boolean isExists = false;
        try {
            isExists = matcher.contains(originalStr, PATTERNS.get(regex));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        if (!isExists) return StringUtils.EMPTY;
        else return StringUtils.trimToEmpty(matcher.getMatch().group(0));
    }

    /**
     * 校验是否手机号码
     * @param text
     * @return
     */
    public static boolean isMobilePhone(String text) {
        return PATTERN_MOBILE.matcher(text).matches();
    }

    /**
     * 校验是否邮箱地址
     * @param text
     * @return
     */
    public static boolean isEmail(String text) {
        return PATTERN_EMAIL.matcher(text).matches();
    }

    /**
     * 校验是否ip地址
     * @param text
     * @return
     */
    public static boolean isIp(String text) {
        return PATTERN_IP.matcher(text).matches();
    }
}
