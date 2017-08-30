package code.ponfee.commons.serial;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import code.ponfee.commons.util.Files;

/**
 * 序例化抽象类
 * @author fupf
 */
public abstract class Serializer {

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
    public abstract <T extends Object> T deserialize(byte[] data, 
                             Class<T> clazz, boolean isCompress);

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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        compress(new ByteArrayInputStream(data), baos);
        return baos.toByteArray();
    }

    /**
     * gzip压缩
     * @param input
     * @param output
     */
    public static void compress(InputStream input, OutputStream output) {
        GZIPOutputStream gzout = null;
        try {
            gzout = new ExtendedGZIPOutputStream(output);
            byte[] buffer = new byte[BUFF_SIZE];
            int len;
            while ((len = input.read(buffer)) != Files.EOF) {
                gzout.write(buffer, 0, len);
            }
            gzout.finish();
            gzout.flush();
            gzout.close();
            gzout = null;
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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        decompress(new ByteArrayInputStream(data), baos);
        return baos.toByteArray();
    }

    /**
     * gzip解压缩
     * @param input
     * @param output
     */
    public static void decompress(InputStream input, OutputStream output) {
        GZIPInputStream gzin = null;
        try {
            gzin = new GZIPInputStream(input);
            IOUtils.copy(gzin, output);
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

    public static void main(String[] args) throws FileNotFoundException {
        //compress(new FileInputStream("d:/test.txt"), new FileOutputStream("d:/test2.txt"));
        decompress(new FileInputStream("d:/test2.txt"), new FileOutputStream("d:/test3.txt"));
    }
}
