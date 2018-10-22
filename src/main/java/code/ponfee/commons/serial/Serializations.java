package code.ponfee.commons.serial;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SerializationUtils;

import code.ponfee.commons.math.Numbers;
import code.ponfee.commons.reflect.ClassUtils;
import code.ponfee.commons.util.Bytes;

/**
 * 公用对象工具类
 * @author Ponfee
 */
public final class Serializations {

    public static byte[] serialize(boolean value) {
        return new byte[] { value ? (byte) 0x01 : (byte) 0x00 };
    }

    public static byte[] serialize(byte value) {
        return new byte[] { value };
    }

    public static byte[] serialize(short value) {
        return Bytes.fromShort(value);
    }

    public static byte[] serialize(char value) {
        return Bytes.fromChar(value);
    }

    public static byte[] serialize(int value) {
        return Bytes.fromInt(value);
    }

    public static byte[] serialize(long value) {
        return Bytes.fromLong(value);
    }

    public static byte[] serialize(float value) {
        return Bytes.fromFloat(value);
    }

    public static byte[] serialize(double value) {
        return Bytes.fromDouble(value);
    }

    public static byte[] serialize(String value) {
        return ((String) value).getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] serialize(Date value) {
        return Bytes.fromLong(((Date) value).getTime());
    }

    public static byte[] serialize(Enum<?> value) {
        return Bytes.fromInt(((Enum<?>) value).ordinal());
    }

    public static byte[] serialize(InputStream value) {
        try (InputStream input = (InputStream) value) {
            return IOUtils.toByteArray(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the serialize byte array data for value
     * 
     * @param value the value
     * @param type  the type of value
     * @return a byte array
     */
    public static byte[] serialize(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof byte[]) {
            return (byte[]) value;
        } else if (value instanceof Byte[]) {
            return ArrayUtils.toPrimitive((Byte[]) value);
        } else if (value instanceof InputStream) {
            try (InputStream input = (InputStream) value) {
                return IOUtils.toByteArray(input);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (value instanceof Boolean) {
            return new byte[] { (Boolean) value ? (byte) 0x01 : (byte) 0x00 };
        } else if (value instanceof Byte) {
            return new byte[] { Numbers.toByte(value) };
        } else if (value instanceof Short) {
            return Bytes.fromShort(Numbers.toShort(value));
        } else if (value instanceof Character) {
            return Bytes.fromChar(Numbers.toChar(value));
        } else if (value instanceof Integer) {
            return Bytes.fromInt(Numbers.toInt(value));
        } else if (value instanceof Long) {
            return Bytes.fromLong(Numbers.toLong(value));
        } else if (value instanceof Float) {
            return Bytes.fromFloat(Numbers.toFloat(value));
        } else if (value instanceof Double) {
            return Bytes.fromDouble(Numbers.toDouble(value));
        } else if (value instanceof String) {
            return ((String) value).getBytes(StandardCharsets.UTF_8);
        } else if (value instanceof Date) {
            return Bytes.fromLong(((Date) value).getTime());
        } else if (value instanceof Enum) {
            return Bytes.fromInt(((Enum<?>) value).ordinal());
        } else if (value instanceof Serializable) {
            return SerializationUtils.serialize((Serializable) value);
        } else {
            throw new UnsupportedOperationException(
                ClassUtils.getClassName(value.getClass()) + " is not Serializable."
            );
        }
    }

    /**
     * Returns a object or primitive value from 
     * deserialize the byte array
     * 
     * @param value the byte array
     * @param type the obj type
     * @return a object
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(byte[] value, Class<T> type) {
        if (value == null || value.length == 0) {
            // type.isPrimitive()
            if (boolean.class == type) {
                return (T) Boolean.FALSE;
            } else if (byte.class == type) {
                //return (T) new Byte((byte) 0);
                return (T) (Byte) (byte) 0;
            } else if (short.class == type) {
                return (T) (Short) (short) 0;
            } else if (char.class == type) {
                return (T) (Character) Numbers.CHAR_ZERO;
            } else if (int.class == type) {
                return (T) (Integer) 0;
            } else if (long.class == type) {
                return (T) (Long) 0L;
            } else if (float.class == type) {
                return (T) (Float) 0F;
            } else if (double.class == type) {
                return (T) (Double) 0D;
            } else if (byte[].class == type) {
                return (T) value;
            } else {
                return null;
            }
        } else if (byte[].class == type) {
            return (T) value;
        } else if (Byte[].class == type) {
            return (T) ArrayUtils.toObject(value);
        } else if (InputStream.class == type) {
            return (T) new ByteArrayInputStream(value);
        }  else if (boolean.class == type || Boolean.class == type) {
            return (T) (value[0] == 0x00 ? Boolean.FALSE : Boolean.TRUE);
        } else if (byte.class == type || Byte.class == type) {
            return (T) (Byte) value[0];
        } else if (short.class == type || Short.class == type) {
            return (T) (Short) Bytes.toShort(value);
        } else if (char.class == type || Character.class == type) {
            return (T) (Character) Bytes.toChar(value);
        } else if (int.class == type || Integer.class == type) {
            return (T) (Integer) Bytes.toInt(value);
        } else if (long.class == type || Long.class == type) {
            return (T) (Long) Bytes.toLong(value);
        } else if (float.class == type || Float.class == type) {
            return (T) (Float) Bytes.toFloat(value);
        } else if (double.class == type || Double.class == type) {
            return (T) (Double) Bytes.toDouble(value);
        } else if (String.class == type) {
            return (T) new String(value, StandardCharsets.UTF_8);
        } else if (Date.class == type) {
            return (T) new Date(Bytes.toLong(value));
        } else if (type.isEnum()) {
            return type.getEnumConstants()[Bytes.toInt(value)];
        } else {
            return (T)SerializationUtils.deserialize(value);
        }
    }

}
