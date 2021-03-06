package code.ponfee.commons.serial;

import org.nustaq.serialization.FSTConfiguration;

import code.ponfee.commons.io.GzipProcessor;
import code.ponfee.commons.reflect.ClassUtils;

/**
 * fst序例化
 * @author fupf
 */
public class FstSerializer extends Serializer {

    // createDefaultConfiguration
    private static final ThreadLocal<FSTConfiguration> FST_CFG =
            ThreadLocal.withInitial(FSTConfiguration::createStructConfiguration);

    @Override
    public byte[] serialize(Object t, boolean isCompress) {
        if (t == null) {
            return null;
        }

        byte[] data = FST_CFG.get().asByteArray(t);
        if (isCompress) {
            data = GzipProcessor.compress(data);
        }
        return data;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz, boolean isCompress) {
        if (data == null) {
            return null;
        }

        if (isCompress) {
            data = GzipProcessor.decompress(data);
        }
        T t = (T) FST_CFG.get().asObject(data);
        if (!clazz.isInstance(t)) {
            throw new ClassCastException(ClassUtils.getClassName(t.getClass())
                     + " can't be cast to " + ClassUtils.getClassName(clazz));
        }
        return t;
    }

}
