package code.ponfee.commons.serial;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 序例化抽象类
 * @author fupf
 */
public abstract class Serializer {

    private static final int EOF = -1;
    static final int BUFF_SIZE = 4096; // 4KB

    private static Logger logger = LoggerFactory.getLogger(Serializer.class);

    /**
     * 对象序例化为流数据
     * @param t 对象
     * @param isCompress 是否要压缩：true是；false否；
     * @return 序例化后的流数据
     */
    public abstract <T extends Object> byte[] serialize(T t, boolean isCompress);

    /**
     * 对象序例化为流数据
     * @param t 对象
     * @return 序例化后的流数据
     */
    public final <T extends Object> byte[] serialize(T t) {
        if (t == null) return null;
        return serialize(t, true);
    }

    /**
     * 流数据反序例化为对象
     * @param data 流数据
     * @param isCompress 是否被压缩：true是；false否；
     * @return 反序例化后的对象
     */
    public abstract <T extends Object> T deserialize(byte[] data, Class<T> clazz, boolean isCompress);

    /**
     * 流数据反序例化为对象
     * @param data 流数据
     * @return 反序例化后的对象
     */
    public final <T extends Object> T deserialize(byte[] data, Class<T> clazz) {
        if (data == null) return null;
        return this.deserialize(data, clazz, true);
    }

    /**
     * 关闭流
     * @param closeable
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) try {
            closeable.close();
        } catch (Exception e) {
            // ignored
        }
    }

    /**
     * 关闭流
     * @param closeable
     * @param error
     */
    public static void close(Closeable closeable, String error) {
        if (closeable != null) try {
            closeable.close();
        } catch (Exception e) {
            logger.error(error, e);
        }
    }

    /**
     * gzip压缩
     * @param data
     * @return
     */
    public static byte[] compress(byte[] data) {
        GZIPOutputStream gzout = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            gzout = new ExtendedGZIPOutputStream(baos);
            gzout.write(data);
            gzout.finish();
            gzout.flush();
            gzout.close();
            gzout = null;
            return baos.toByteArray();
        } catch (IOException e) {
            throw new SerializationException(e);
        } finally {
            if (gzout != null) try {
                gzout.close();
            } catch (IOException e) {
                logger.error("close GZIPOutputStream exception", e);
            }
        }
    }

    /**
     * gzip解压缩
     * @param data
     * @return
     */
    public static byte[] decompress(byte[] data) {
        GZIPInputStream gzin = null;
        try {
            gzin = new GZIPInputStream(new ByteArrayInputStream(data));
            return toByteArray(gzin);
        } catch (IOException e) {
            throw new SerializationException(e);
        } finally {
            if (gzin != null) try {
                gzin.close();
            } catch (IOException e) {
                logger.error("close GZIPInputStream exception", e);
            }
        }
    }

    /**
     * 将InputStream转换成byte数组
     * @param in InputStream
     * @return byte[]
     * @throws IOException
     */
    public static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buff = new byte[BUFF_SIZE];
        for (int n; (n = in.read(buff)) != EOF;) {
            out.write(buff, 0, n);
        }
        return out.toByteArray();
    }

    /**
     * 扩展自GZIPOutputStream
     */
    static class ExtendedGZIPOutputStream extends GZIPOutputStream {
        ExtendedGZIPOutputStream(OutputStream out) throws IOException {
            this(out, Deflater.DEFAULT_COMPRESSION);
        }

        ExtendedGZIPOutputStream(OutputStream out, int level) throws IOException {
            super(out);
            super.def.setLevel(level);
        }
    }

}
