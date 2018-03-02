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

    /** Jackson ObjectMapper(thread safe) */
    private final ObjectMapper mapper = new ObjectMapper();

    private Jsons(JsonInclude.Include include) {
        // 设置序列化时的特性
        if (include != null) {
            mapper.setSerializationInclusion(include);
        }

        // 反序列化时忽略不存在于对象中的属性
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        /*mapper.enable(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        mapper.enable(com.fasterxml.jackson.core.JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        //mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy(){
            private static final long serialVersionUID = -3401320843245849044L;
            // todo
        });
        mapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));*/
    }

    /**
     * convert an object(POJO, Array, Collection, ...) to json string
     * @param target target object
     * @return json string
     * @throws code.ponfee.commons.json.JsonException   the exception for json
     */
    public String stringify(Object target) throws JsonException {
        try {
            return mapper.writeValueAsString(target);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    /**
     * serialize the byte array of json
     * @param target  object
     * @return byte[] array
     * @throws code.ponfee.commons.json.JsonException   the exception for json
     */
    public byte[] serialize(Object target) throws JsonException {
        try {
            return mapper.writeValueAsBytes(target);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    /**
     * deserialize a json to target class object
     * @param json json string
     * @param target target class
     * @return target object
     * @throws code.ponfee.commons.json.JsonException   the exception for json
     */
    public <T> T parse(String json, Class<T> target) throws JsonException {
        if (isEmpty(json)) {
            return null;
        }

        try {
            return mapper.readValue(json, target);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    /**
     * 反序列化集合对象
     * @param json          the json string
     * @param collectClass  the collection class type
     * @param elemClasses   the element class type
     * @return the objects of collection
     * @throws JsonException the exception for json
     */
    public <T> T parse(String json, Class<T> collectClass, Class<?>... elemClasses)
        throws JsonException {
        return parse(json, createCollectionType(collectClass, elemClasses));
    }

    /**
     * 反序列化
     * @param json json string
     * @param javaType JavaType
     * @return the javaType's object
     * @throws JsonException the exception for json
     * @see #createCollectionType(Class, Class...)
     */
    public <T> T parse(String json, JavaType javaType) throws JsonException {
        if (isEmpty(json)) {
            return null;
        }

        try {
            return mapper.readValue(json, javaType);
        } catch (Exception e) {
            throw new JsonException(e);
        }
    }

    /**
     * construct collection type
     * @param collecClass collection class, such as ArrayList, HashMap, ...
     * @param elemClasses element class
     * @return JavaType
     */
    public JavaType createCollectionType(Class<?> collecClass, 
                                         Class<?>... elemClasses) {
        return mapper.getTypeFactory()
                     .constructParametricType(collecClass, elemClasses);
    }

    public static String toJson(Object target) {
        return NORMAL.stringify(target);
    }

    public <T> T fromJson(String json, Class<T> target) {
        return NORMAL.parse(json, target);
    }

    private static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
