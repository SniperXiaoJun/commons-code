package code.ponfee.commons.reflect;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * 泛型工具类
 * @author fupf
 */
public final class GenericUtils {

    private GenericUtils() {}

    /**
     * map泛型协变
     * @param origin
     * @return
     */
    public static Map<String, String> covariant(Map<String, ?> origin) {
        if (origin == null) return null;
        Map<String, String> target = new HashMap<>();
        for (Entry<String, ?> entry : origin.entrySet()) {
            target.put(entry.getKey(), Objects.toString(entry.getValue(), null));
        }
        return target;
    }

}
