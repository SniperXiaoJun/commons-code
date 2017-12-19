package code.ponfee.commons.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 消息格式化
 * @author fupf
 */
public final class MessageFormats {
    private MessageFormats() {}

    private static final String PREFIX = "#\\{(\\s|\\t)*";
    private static final String SUFFIX = "(\\s|\\t)*\\}";
    private static final Pattern PATTERN = Pattern.compile(PREFIX + "(\\w+)" + SUFFIX);

    public static String format(String text, Map<String, Object> args) {
        List<Object> objs = new ArrayList<>(args.size());
        int i = 0;
        for (Entry<String, Object> entry : args.entrySet()) {
            text = text.replaceAll(PREFIX + entry.getKey() + SUFFIX, "{" + i++ + "}");
            objs.add(String.valueOf(entry.getValue()));
        }
        return MessageFormat.format(text, objs.toArray());
    }

    public static String format(String text, Object... args) {
        Map<String, Object> map = new HashMap<>(args.length * 2);
        Matcher matcher = PATTERN.matcher(text);
        for (int n = args.length, i = 0; i < n && matcher.find(); i++) {
            map.put(matcher.group(2), args[i]);
        }
        return format(text, map);
    }

    public static String formatPair(String text, Object... args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("args must be pair.");
        }
        Map<String, Object> map = new HashMap<>(args.length * 2);
        for (int n = args.length, i = 0; i < n; i += 2) {
            map.put(args[i].toString(), args[i + 1]);
        }
        return format(text, map);
    }

    public static void main(String[] args) {
        System.out.println(format("#{     a}|#{ b   }|#{word  }", "#{a}#{b}#{word}", "#{a}#{b}#{word}", "#{word  }"));
    }

}
