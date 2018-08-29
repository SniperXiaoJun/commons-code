package code.ponfee.commons.serial;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoPool;

import code.ponfee.commons.io.ExtendedGZIPOutputStream;
import code.ponfee.commons.io.Files;

/**
 * kryo序例化
 * 
 * the bean class must include default no-arg constructor
 * 
 * @author fupf
 */
public class KryoSerializer extends Serializer {

    private static Logger logger = LoggerFactory.getLogger(KryoSerializer.class);

    private final KryoPool kryoPool;

    public KryoSerializer() {
        this.kryoPool = new KryoPool.Builder(Kryo::new).softReferences().build();
    }

    @Override
    public byte[] serialize(Object t, boolean isCompress) {
        if (t == null) {
            return null;
        }

        GZIPOutputStream gzout = null;
        Output output = null;
        Kryo kryo = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(BYTE_SIZE);
            if (isCompress) {
                gzout = new ExtendedGZIPOutputStream(baos);
                output = new ByteBufferOutput(gzout, Files.BUFF_SIZE);
            } else {
                output = new ByteBufferOutput(baos, Files.BUFF_SIZE);
            }
            (kryo = getKryo()).writeObject(output, t);
            output.close();
            output = null;
            if (gzout != null) {
                gzout.close();
                gzout = null;
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new SerializationException(e);
        } finally {
            this.releaseKryo(kryo);
            close(output, "close Output exception");
            close(gzout, "close GZIPOutputStream exception");
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz, boolean isCompress) {
        if (data == null) {
            return null;
        }

        GZIPInputStream gzin = null;
        Input input = null;
        Kryo kryo = null;
        try {
            if (isCompress) {
                gzin = new GZIPInputStream(new ByteArrayInputStream(data));
                input = new ByteBufferInput(gzin);
            } else {
                input = new ByteBufferInput(data);
            }
            return (kryo = getKryo()).readObject(input, clazz);
        } catch (IOException e) {
            throw new SerializationException(e);
        } finally {
            this.releaseKryo(kryo);
            close(input, "close Input exception");
            close(gzin, "close GZIPInputStream exception");
        }
    }

    private Kryo getKryo() {
        return this.kryoPool.borrow();
    }

    private void releaseKryo(Kryo kryo) {
        if (kryo != null) try {
            this.kryoPool.release(kryo);
        } catch (Throwable t) {
            logger.error("release kryo occur error", t);
        }
    }
}
