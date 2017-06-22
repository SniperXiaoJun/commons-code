package code.ponfee.commons.serial;

import org.nustaq.serialization.FSTConfiguration;

import code.ponfee.commons.reflect.ClassUtils;

/**
 * fst序例化
 * @author fupf
 */
public class FstSerializer extends Serializer {

    private static final ThreadLocal<FSTConfiguration> FST_CFG = new ThreadLocal<FSTConfiguration>() {
        public synchronized FSTConfiguration initialValue() {
            return FSTConfiguration.createDefaultConfiguration();
        }
    };

    public <T extends Object> byte[] serialize(T t, boolean isCompress) {
        if (t == null) return null;
        byte[] data = FST_CFG.get().asByteArray(t);
        if (isCompress) data = compress(data);
        return data;
    }

    @SuppressWarnings("unchecked")
    public <T extends Object> T deserialize(byte[] data, Class<T> clazz, boolean isCompress) {
        if (data == null) return null;

        if (isCompress) data = decompress(data);
        T t = (T) FST_CFG.get().asObject(data);
        if (clazz != t.getClass()) {
            throw new IllegalArgumentException("expect " + ClassUtils.getClassName(clazz) 
            + " type, but it's " + ClassUtils.getClassName(t.getClass()) + " type");
        }
        return t;
    }

}
