package code.ponfee.commons.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据流转换工具类
 * @author fupf
 */
public final class Streams {
    private static Logger logger = LoggerFactory.getLogger(Streams.class);
    private static final int BUFFER_SIZE = 0x1000; // 缓冲大小4096
    private static final byte[] WITH_BOM = { (byte) 0xEF, (byte) 0XBB, (byte) 0XBF };

    /**
     * byte[]存文件
     * @param datas
     * @param targetPath
     * @throws IOException
     */
    public static void bytes2file(byte[] datas, String targetPath) throws IOException {
        try ( OutputStream out = new FileOutputStream(targetPath);
              BufferedOutputStream bos = new BufferedOutputStream(out, BUFFER_SIZE);
        ) {
            bos.write(datas);
            bos.flush();
        }
    }

    /**
     * 把文件转为byte数组
     * @param path
     * @return
     * @throws IOException
     */
    public static byte[] file2bytes(String path) throws IOException {
        return input2bytes(new FileInputStream(path));
    }

    public static byte[] file2bytes(File file) throws IOException {
        return input2bytes(new FileInputStream(file));
    }

    /**
     * 将InputStream转换成byte数组
     * @param input InputStream
     * @return byte[]
     * @throws IOException
     */
    public static byte[] input2bytes(InputStream input) throws IOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] data = new byte[BUFFER_SIZE];
            int count;
            while ((count = input.read(data, 0, BUFFER_SIZE)) != -1) {
                baos.write(data, 0, count);
            }
            return baos.toByteArray();
        } finally {
            if (input != null) try {
                input.close();
            } catch (IOException e) {
                logger.error("inputstream转byte流关闭流时出错", e);
            }
        }
    }

    /**
     * 将字符串保存到文件
     * @param str
     * @param targetPath
     * @throws IOException
     */
    public static void string2file(String str, String targetPath, String charset) throws IOException {
        BufferedWriter writer = null;
        OutputStreamWriter osw = null;
        try (FileOutputStream fos = new FileOutputStream(targetPath)) {
            if (charset != null && charset.trim().length() > 0) {
                osw = new OutputStreamWriter(fos, charset);
            } else {
                osw = new OutputStreamWriter(fos);
            }
            writer = new BufferedWriter(osw, BUFFER_SIZE);
            writer.write(str);
            writer.flush();
        } finally {
            if (osw != null) try {
                osw.close();
            } catch (IOException e) {
                logger.error("关闭输出流时出错", e);
            }
            if (writer != null) try {
                writer.close();
            } catch (IOException e) {
                logger.error("关闭输出流时出错", e);
            }
        }
    }

    /**
     * 增加BOM
     * @param filepath
     */
    public static void addBOM(String filepath) {
        addBOM(new File(filepath));
    }

    public static void addBOM(File file) {
        FileOutputStream output = null;
        BufferedOutputStream bos = null;
        try (FileInputStream input = new FileInputStream(file)) {
            int length = input.available();
            byte[] bytes1, bytes2;
            if (length >= 3) {
                bytes1 = new byte[3];
                input.read(bytes1);
                if (ObjectUtils.equals(WITH_BOM, bytes1)) {
                    return;
                }
                bytes2 = new byte[length - 3];
                input.read(bytes2);
            } else {
                bytes1 = new byte[0];
                bytes2 = new byte[length];
                input.read(bytes2);
            }
            output = new FileOutputStream(file);
            bos = new BufferedOutputStream(output);
            bos.write(WITH_BOM);
            bos.write(bytes1);
            bos.write(bytes2);
            bos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (bos != null) try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (output != null) try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 移除BOM
     * @param filepath
     */
    public static void removeBOM(String filepath) {
        removeBOM(new File(filepath));
    }

    public static void removeBOM(File file) {
        FileOutputStream output = null;
        BufferedOutputStream bos = null;
        try (FileInputStream input = new FileInputStream(file)) {
            int length = input.available();
            if (length <= 3) return;

            byte[] bytes = new byte[3];
            input.read(bytes);
            if (!ObjectUtils.equals(bytes, WITH_BOM)) return;

            bytes = new byte[length - 3];
            input.read(bytes);
            output = new FileOutputStream(file);
            bos = new BufferedOutputStream(output);
            //bos.write(bytes, 3, bytes.length - 3);
            bos.write(bytes);
            bos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (bos != null) try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (output != null) try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将字符串保存到文件
     * @param str
     * @param targetPath
     * @throws IOException
     */
    public static void string2file(String str, String targetPath) throws IOException {
        string2file(str, targetPath, null);
    }

    /**
     * 流转字符
     * @param in
     * @return
     * @throws IOException
     */
    public static String input2string(InputStream in, String charset) throws IOException {
        BufferedReader reader = null;
        InputStreamReader isr = null;
        try {
            if (charset != null && charset.trim().length() > 0) {
                isr = new InputStreamReader(in, charset);
            } else {
                isr = new InputStreamReader(in);
            }
            reader = new BufferedReader(isr, BUFFER_SIZE);
            StringBuffer buf = new StringBuffer();
            String s = null;
            while ((s = reader.readLine()) != null) {
                buf.append(s).append("\n");
            }
            return buf.toString();
        } finally {
            if (reader != null) try {
                reader.close();
            } catch (IOException e) {
                logger.error("关闭输入流时出错", e);
            }
            if (isr != null) try {
                isr.close();
            } catch (IOException e) {
                logger.error("关闭输入流时出错", e);
            }
            if (in != null) try {
                in.close();
            } catch (IOException e) {
                logger.error("关闭输入流时出错", e);
            }
        }
    }

    /**
     * 流转字符
     * @param in
     * @param charset
     * @return
     * @throws IOException
     */
    public static String input2string(InputStream in) throws IOException {
        return input2string(in, null);
    }

    /**
     * 读取文件数据为字符串
     * @param filePath
     * @return
     * @throws IOException
     */
    public static String file2string(String filePath) throws IOException {
        return input2string(new FileInputStream(filePath), null);
    }

    public static String file2string(File file) throws IOException {
        return input2string(new FileInputStream(file), null);
    }

    /**
     * 流转字符
     * @param filePath
     * @param charset
     * @return
     * @throws IOException
     */
    public static String file2string(String filePath, String charset) throws IOException {
        return input2string(new FileInputStream(filePath), charset);
    }

    public static void main(String[] args) {
        byte[] b = new byte[8];
        ThreadLocalRandom.current().nextBytes(b);
        System.out.println(Bytes.hexEncode(b));
        String b64 = Bytes.base64EncodeUrlSafe(b);
        System.out.println(Objects.deepEquals(Bytes.base64DecodeUrlSafe(b64), b));

        addBOM(new File("d:/csv.csv"));
    }
}
