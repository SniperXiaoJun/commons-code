package code.ponfee.commons.serial;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import code.ponfee.commons.io.GzipProcessor;

/**
 * 序例化抽象类
 * @author fupf
 */
public abstract class Serializer {

    static final int BYTE_SIZE = 512;

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
        if (t == null) {
            return null;
        }

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
        if (data == null) {
            return null;
        }

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

    public static void main(String[] args) throws FileNotFoundException {
        //GzipCompressor.compress(new FileInputStream("d:/test.txt"), new FileOutputStream("d:/test2.txt"));
        GzipProcessor.decompress(new FileInputStream("d:/test2.txt"), new FileOutputStream("d:/test3.txt"));
    }
}
