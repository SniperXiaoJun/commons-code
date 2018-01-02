package code.ponfee.commons.serial;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caucho.hessian.io.HessianSerializerInput;
import com.caucho.hessian.io.HessianSerializerOutput;

import code.ponfee.commons.io.ExtendedGZIPOutputStream;
import code.ponfee.commons.reflect.ClassUtils;

/**
 * hessian序例化
 * @author fupf
 */
public class HessianSerializer extends Serializer {

    private static Logger logger = LoggerFactory.getLogger(HessianSerializer.class);

    public <T extends Object> byte[] serialize(T t, boolean isCompress) {
        if (t == null) {
            return null;
        }

        GZIPOutputStream gzout = null;
        HessianSerializerOutput hessian = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(BYTE_SIZE);
            if (isCompress) {
                gzout = new ExtendedGZIPOutputStream(baos);
                hessian = new HessianSerializerOutput(gzout);
            } else {
                hessian = new HessianSerializerOutput(baos);
            }
            hessian.writeObject(t);
            hessian.flush();
            hessian.close();
            hessian = null;
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
            if (hessian != null) try {
                hessian.close();
            } catch (IOException e) {
                logger.error("close hessian exception", e);
            }
            close(gzout, "close GZIPOutputStream exception");
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Object> T deserialize(byte[] data, Class<T> clazz, boolean isCompress) {
        if (data == null) {
            return null;
        }

        GZIPInputStream gzin = null;
        HessianSerializerInput hessian = null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            if (isCompress) {
                gzin = new GZIPInputStream(bais);
                hessian = new HessianSerializerInput(gzin);
            } else {
                hessian = new HessianSerializerInput(bais);
            }
            T t = (T) hessian.readObject();
            if (!clazz.isInstance(t)) {
                throw new ClassCastException(ClassUtils.getClassName(t.getClass())
                         + " can't be cast to " + ClassUtils.getClassName(clazz));
            }
            return t;
        } catch (IOException e) {
            throw new SerializationException(e);
        } finally {
            if (hessian != null) try {
                hessian.close();
            } catch (Exception e) {
                logger.error("close hessian exception", e);
            }
            close(gzin, "close GZIPInputStream exception");
        }
    }

}
