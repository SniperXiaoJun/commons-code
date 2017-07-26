package code.ponfee.commons.serial;

import java.nio.charset.Charset;
import java.util.HashMap;

import code.ponfee.commons.reflect.ClassUtils;

/**
 * 字段串序例化
 * @author fupf
 */
public class StringSerializer extends Serializer {

    private final Charset charset;

    public StringSerializer() {
        this(Charset.forName("UTF8"));
    }

    public StringSerializer(Charset charset) {
        if (charset == null) {
            throw new IllegalArgumentException("charset can not be null");
        }
        this.charset = charset;
    }

    public StringSerializer(String charset) {
        this.charset = Charset.forName(charset);
    }

    @Override
    public <T> byte[] serialize(T t, boolean isCompress) {
        if (t == null) {
            return null;
        } else if (t instanceof String) {
            byte[] data = ((String) t).getBytes(charset);
            if (isCompress) data = compress(data);
            return data;
        } else {
            throw new SerializationException("object must be java.lang.String type, but it's "
                + ClassUtils.getClassName(t.getClass()) + " type");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] data, Class<T> clazz, boolean isCompress) {
        if (clazz != String.class) {
            throw new SerializationException("argument class must be java.lang.String.class, but it's "
                + ClassUtils.getClassName(clazz) + ".class");
        }

        if (data == null) return null;

        if (isCompress) data = decompress(data);
        return (T) new String(data, charset);
    }

    public static void main(String[] args) {
        Serializer serializer = new StringSerializer();
        byte[] data = serializer.serialize("abcde");
        serializer.deserialize(data, HashMap.class);
    }
}
