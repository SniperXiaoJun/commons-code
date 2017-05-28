package code.ponfee.commons.serial;

import org.nustaq.serialization.FSTConfiguration;

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
        if (!clazz.equals(t.getClass())) {
            throw new IllegalArgumentException(t.getClass().getCanonicalName()
                + " not equal to " + clazz.getCanonicalName());
        }
        return t;
    }

}
