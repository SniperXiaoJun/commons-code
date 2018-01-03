package code.ponfee.commons.serial;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import code.ponfee.commons.io.ExtendedGZIPOutputStream;
import code.ponfee.commons.reflect.ClassUtils;

/**
 * java序例化
 * @author fupf
 */
public class JdkSerializer extends Serializer {

    @Override
    public <T extends Object> byte[] serialize(T t, boolean isCompress) {
        if (t == null) {
            return null;
        }

        GZIPOutputStream gzout = null;
        ObjectOutputStream oos = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(BYTE_SIZE);
            if (isCompress) {
                gzout = new ExtendedGZIPOutputStream(baos);
                oos = new ObjectOutputStream(gzout);
            } else {
                oos = new ObjectOutputStream(baos);
            }
            oos.writeObject(t);
            oos.flush();
            oos.close();
            oos = null;
            if (gzout != null) {
                gzout.finish();
                gzout.flush();
                gzout.close();
                gzout = null;
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new SerializationException(e);
        } finally {
            // 先打开的后关闭，后打开的先关闭
            // 看依赖关系，如果流a依赖流b，应该先关闭流a，再关闭流b
            // 处理流a依赖节点流b，应该先关闭处理流a，再关闭节点流b
            close(oos, "close ObjectOutputStream exception");
            close(gzout, "close GZIPOutputStream exception");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Object> T deserialize(byte[] data, Class<T> clazz, boolean isCompress) {
        if (data == null || data.length == 0) {
            return null;
        }

        GZIPInputStream gzin = null;
        ObjectInputStream ois = null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            if (isCompress) {
                gzin = new GZIPInputStream(bais);
                ois = new ObjectInputStream(gzin);
            } else {
                ois = new ObjectInputStream(bais);
            }

            T t = (T) ois.readObject();
            if (!clazz.isInstance(t)) {
                throw new ClassCastException(ClassUtils.getClassName(t.getClass())
                         + " can't be cast to " + ClassUtils.getClassName(clazz));
            }
            return t;
        } catch (IOException | ClassNotFoundException e) {
            throw new SerializationException(e);
        } finally {
            close(ois, "close ObjectInputStream exception");
            close(gzin, "close GZIPInputStream exception");
        }
    }

}
