package code.ponfee.commons.util;

import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
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
public final class Regexs {
    private Regexs() {}

    private static final LoadingCache<String, Pattern> PATTERNS =
        CacheBuilder.newBuilder().softValues().build(new CacheLoader<String, Pattern>() {
            @Override
            public Pattern load(String pattern) throws Exception {
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
}
