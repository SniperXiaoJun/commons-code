package code.ponfee.commons.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 扩展自MessageFormat的消息格式化
 * @author fupf
 */
public class ExtendedMessageFormat extends MessageFormat {
    private static final long serialVersionUID = -2531373358944157875L;

    public ExtendedMessageFormat(String pattern) {
        super(pattern);
    }

    /**
     * 格式化
     * @param pattern
     * @param args
     * @return
     */
    public static String format(String pattern, Map<String, Object> args) {
        List<Object> objs = new ArrayList<>();
        int i = 0;
        for (Entry<String, Object> entry : args.entrySet()) {
            pattern = pattern.replaceAll("#\\{(\\s|\\t)*" + entry.getKey() + "(\\s|\\t)*\\}", "{" + i++ + "}");
            objs.add(String.valueOf(entry.getValue()));
        }
        return MessageFormat.format(pattern, objs.toArray());
    }

    public static String formatPair(String key, Object... params) {
        if (params.length % 2 != 0) {
            throw new IllegalArgumentException("格式化参数不成对");
        }
        Map<String, Object> map = new HashMap<String, Object>();
        for (int j = 0; j < params.length; j += 2) {
            map.put(params[j].toString(), params[j + 1]);
        }
        return format(key, map);
    }

}
