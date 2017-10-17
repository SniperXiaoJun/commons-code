package code.ponfee.commons.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.output.StringBuilderWriter;

import code.ponfee.commons.util.ObjectUtils;

/**
 * 文件工具类
 * @author Ponfee
 */
public final class Files {
    private Files() {}

    public static final int EOF = -1;
    public static final String SEPARATOR = "/";
    public static final int BUFF_SIZE = 4096;
    public static final String LINE_SEPARATOR;
    static {
        /*String separator = (String) AccessController.doPrivileged(new GetPropertyAction("line.separator"));
        if (StringUtils.isEmpty(separator)) separator = System.getProperty("line.separator", "/n");
        LINE_SEPARATOR = separator;*/
        final StringBuilderWriter buf = new StringBuilderWriter(4);
        final PrintWriter out = new PrintWriter(buf);
        out.println();
        LINE_SEPARATOR = buf.toString();
        out.close();
    }

    /** 
     * 文件大小可读化（attach unit）：B、KB、MB
     * @param size 文件字节大小 
     */
    private static final String[] FILE_UNITS = { "B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB" };
    public static String human(long size) {
        if (size <= 0) return "0";

        int digit = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digit)) + FILE_UNITS[digit];
    }

    /**
     * 创建目录
     * @param path
     */
    public static File mkdir(String path) {
        return mkdir(new File(path));
    }

    /**
     * 创建目录
     * @param file
     * @return
     */
    public static File mkdir(File file) {
        if (!file.exists()) {
            file.mkdirs();
        } else if (file.isFile()) {
            throw new IllegalStateException("file [" + file.getAbsolutePath() + "] not a dir");
        }
        return file;
    }

    /**
     * 创建文件
     * @param file
     * @return
     */
    public static File touch(String file) {
        return touch(new File(file));
    }

    /**
     * 创建文件
     * @param file
     */
    public static File touch(File file) {
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        } else if (file.isDirectory()) {
            throw new IllegalStateException("dir [" + file.getAbsolutePath() + "] not a file");
        }
        return file;
    }

    /**
     * read file as string
     * @param file
     * @return
     */
    public static String toString(String file) {
        return toString(new File(file));
    }

    public static String toString(File file) {
        return toString(file, Charset.defaultCharset().name());
    }

    public static String toString(File file, String charset) {
        try (FileInputStream in = new FileInputStream(file); 
             FileChannel channel = in.getChannel();
        ) {
            ByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, channel.size());
            return Charset.forName(charset).decode(buffer).toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * read file to byte array
     * @param file
     * @return
     */
    public static byte[] toByteArray(File file) {
        try (FileInputStream in = new FileInputStream(file); 
             FileChannel channel = in.getChannel();
        ) {
            ByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, channel.size());
            byte[] bytes = new byte[buffer.capacity()];
            buffer.get(bytes, 0, bytes.length);
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 读取文件全部行数据
     * @param input
     * @return
     */
    public static List<String> readLines(InputStream input) {
        return readLines(input, null);
    }

    /**
     * 读取文件全部行数据
     * @param input
     * @param charset
     * @return
     */
    public static List<String> readLines(InputStream input, String charset) {
        List<String> list = new ArrayList<>();
        try (Scanner scanner = (charset == null) 
            ? new Scanner(input) 
            : new Scanner(input, charset)
        ) {
            while (scanner.hasNextLine()) {
                list.add(scanner.nextLine());
            }
        }
        return list;
    }

    // -------------------------------------windows file bom head-------------------------------------
    /**
     * add file bom head
     * @param filepath
     */
    private static final byte[] WITH_BOM = { (byte) 0xEF, (byte) 0XBB, (byte) 0XBF };
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
     * remove file bom head
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
            if (length < 3) return;

            byte[] bytes = new byte[3];
            input.read(bytes);
            if (!ObjectUtils.equals(bytes, WITH_BOM)) return;

            bytes = new byte[length - 3];
            input.read(bytes);
            output = new FileOutputStream(file);
            bos = new BufferedOutputStream(output);
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

}
