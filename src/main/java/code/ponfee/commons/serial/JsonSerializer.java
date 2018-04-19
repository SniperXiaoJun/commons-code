package code.ponfee.commons.serial;

import code.ponfee.commons.io.ExtendedGZIPOutputStream;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * json序例化
 * @author fupf
 */
public class JsonSerializer extends Serializer {

    /** json object mapper */
    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public <T extends Object> byte[] serialize(T t, boolean isCompress) {
        if (t == null) {
            return null;
        }

        GZIPOutputStream gzout = null;
        try {
            byte[] data = MAPPER.writeValueAsBytes(t);
            if (isCompress) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream(BYTE_SIZE);
                gzout = new ExtendedGZIPOutputStream(baos);
                gzout.write(data, 0, data.length);
                gzout.close();
                gzout = null;
                data = baos.toByteArray();
            }
            return data;
        } catch (IOException e) {
            throw new SerializationException(e);
        } finally {
            close(gzout, "close GZIPOutputStream exception");
        }
    }

    @Override
    public <T extends Object> T deserialize(byte[] data, Class<T> clazz, boolean isCompress) {
        if (data == null) {
            return null;
        }

        GZIPInputStream gzin = null;
        try {
            if (isCompress) {
                gzin = new GZIPInputStream(new ByteArrayInputStream(data));
                data =  IOUtils.toByteArray(gzin);
            }
            return MAPPER.readValue(data, clazz);
        } catch (IOException e) {
            throw new SerializationException(e);
        } finally {
            close(gzin, "close GZIPInputStream exception");
        }
    }

}
