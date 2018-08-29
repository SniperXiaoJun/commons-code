package code.ponfee.commons.serial;

import java.io.Closeable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public abstract byte[] serialize(Object t, boolean isCompress);

    /**
     * 对象序例化为流数据
     * @param t 对象
     * @return 序例化后的流数据
     */
    public final byte[] serialize(Object t) {
        if (t == null) {
            return null;
        }

        return serialize(t, false);
    }

    /**
     * 流数据反序例化为对象
     * @param data 流数据
     * @param isCompress 是否被压缩：true是；false否；
     * @return 反序例化后的对象
     */
    public abstract <T> T deserialize(byte[] data, Class<T> clazz, 
                                      boolean isCompress);

    /**
     * 流数据反序例化为对象
     * @param data 流数据
     * @return 反序例化后的对象
     */
    public final <T> T deserialize(byte[] data, Class<T> clazz) {
        if (data == null) {
            return null;
        }

        return this.deserialize(data, clazz, false);
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

}
