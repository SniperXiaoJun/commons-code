package code.ponfee.commons.serial;

import code.ponfee.commons.io.GzipProcessor;

/**
 * 
 * Wrapped Serializer
 * 
 * @author fupf
 */
public class WrappedSerializer extends Serializer {

    @Override
    public byte[] serialize(Object obj, boolean isCompress) {
        if (obj == null) {
            return null;
        }

        byte[] data = Serializations.serialize(obj);
        if (isCompress) {
            data = GzipProcessor.compress(data);
        }
        return data;
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> type, boolean isCompress) {
        if (data == null) {
            return null;
        }

        if (isCompress) {
            data = GzipProcessor.decompress(data);
        }
        return Serializations.deserialize(data, type);
    }

}
