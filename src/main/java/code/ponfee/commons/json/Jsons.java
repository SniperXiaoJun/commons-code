package code.ponfee.commons.json;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 基于jackson的json工具类
 * @author fupf
 */
public final class Jsons {

    /** 标准（不排除任何属性） */
    public static final Jsons NORMAL = new Jsons(null);

    /** 忽略对象中值为 null */
    public static final Jsons NON_NULL = new Jsons(JsonInclude.Include.NON_NULL);

    /** 忽略对象中值为 null 或 "" 的属性 */
    public static final Jsons NON_EMPTY = new Jsons(JsonInclude.Include.NON_EMPTY);

    /** 忽略对象中值为默认值的属性（慎用） */
    public static final Jsons NON_DEFAULT = new Jsons(JsonInclude.Include.NON_DEFAULT);

    /** Jackson ObjectMapper */
    private final ObjectMapper mapper = new ObjectMapper();

    private Jsons(JsonInclude.Include include) {
        // 设置序列化时的特性
        if (include != null) mapper.setSerializationInclusion(include);

        // ignore attributes exists in json string, but not in java object when deserialization
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        /*mapper.enable(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        mapper.enable(com.fasterxml.jackson.core.JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy(){
            private static final long serialVersionUID = -3401320843245849044L;
            // todo
        });
        mapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));*/
    }

    /**
     * 序列化 convert an object(POJO, Collection, ...) to json string
     * @param target target object
     * @return json string
     * @throws code.ponfee.commons.json.JsonException the exception for json
     */
    public String stringify(Object target) {
        try {
            return mapper.writeValueAsString(target);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    /**
     * 反序列化 deserialize a json to target class object
     * @param json json string
     * @param target target class
     * @param <T> the generic type
     * @return target object
     * @throws JsonException the exception for json
     */
    public <T> T parse(String json, Class<T> target) {
        if (isEmpty(json)) return null;

        try {
            return mapper.readValue(json, target);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    /**
     * 反序列化
     * @param javaType JavaType
     * @param jsonString json string
     * @param <T> the generic type
     * @see #createCollectionType(Class, Class...)
     * @return the javaType's object
     * @throws JsonException the exception for json
     */
    public <T> T parse(String jsonString, JavaType javaType) {
        if (isEmpty(jsonString)) return null;

        try {
            return mapper.readValue(jsonString, javaType);
        } catch (Exception e) {
            throw new JsonException(e);
        }
    }

    /**
     * construct collection type
     * @param collectionClass collection class, such as ArrayList, HashMap, ...
     * @param elementClasses element class
     * @return JavaType
     */
    public JavaType createCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }

    private static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
}
