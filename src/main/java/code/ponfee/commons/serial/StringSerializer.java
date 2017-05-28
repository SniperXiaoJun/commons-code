package code.ponfee.commons.serial;

import java.nio.charset.Charset;

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

    @Override
    public <T> byte[] serialize(T t, boolean isCompress) {
        if (t == null) {
            return null;
        } else if (t instanceof String) {
            byte[] data = ((String) t).getBytes(charset);
            if (isCompress) data = compress(data);
            return data;
        } else {
            throw new SerializationException("must be string data type");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] data, Class<T> clazz, boolean isCompress) {
        if (data == null) return null;
        if (clazz != String.class) {
            throw new SerializationException("clazz must be String.class");
        }
        if (isCompress) data = decompress(data);
        return (T) new String(data, charset);
    }

}
