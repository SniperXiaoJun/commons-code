package code.ponfee.commons.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.ImmutableMap;

import code.ponfee.commons.util.Bytes;
import code.ponfee.commons.util.ObjectUtils;

/**
 * 文件工具类
 * @author Ponfee
 */
public final class Files {
    private Files() {}

    public static final int EOF = -1; // end of file read

    public static final String FILE_SEPARATOR = "/"; // file path separator

    public static final int BUFF_SIZE = 4096; // file buffer size

    public static final String LINE_SEPARATOR; // line separator of file
    static {
        /*String separator = java.security.AccessController.doPrivileged(
               new sun.security.action.GetPropertyAction("line.separator"));
        if (StringUtils.isEmpty(separator)) {
            separator = System.getProperty("line.separator", "/n");
        }
        LINE_SEPARATOR = separator;*/
        final StringBuilderWriter buffer = new StringBuilderWriter(4);
        final PrintWriter out = new PrintWriter(buffer);
        out.println();
        LINE_SEPARATOR = buffer.toString();
        out.close();
    }

    private static final String[] FILE_UNITS = { "B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB" };
    /** 
     * 文件大小可读化（attach unit）：B、KB、MB
     * @param size 文件字节大小 
     * @return
     */
    public static String human(long size) {
        if (size <= 0) return "0";

        int digit = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digit)) + FILE_UNITS[digit];
    }

    /**
     * 创建目录
     * @param path
     * @return
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
     * @return
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

    public static List<String> readLines(File file) throws FileNotFoundException {
        return readLines(new FileInputStream(file), null);
    }

    public static List<String> readLines(File file, String charset) throws FileNotFoundException {
        return readLines(new FileInputStream(file), charset);
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

    // ------------------------file type---------------------------------
    private static final int SUB_PREFIX = 64;
    public static final Map<String, String> FILE_TYPE_MAGIC;
    static {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        builder.put("jpg", "FFD8FF"); // JPEG (jpg)
        builder.put("png", "89504E47"); // PNG (png)
        builder.put("gif", "47494638"); // GIF (gif)
        builder.put("tif", "49492A00"); // TIFF (tif)
        builder.put("bmp", "424D"); // Windows Bitmap (bmp)
        builder.put("dwg", "41433130"); // CAD (dwg)
        builder.put("html","68746D6C3E"); // HTML (html)
        builder.put("rtf", "7B5C727466"); // Rich Text Format (rtf)
        builder.put("xml", "3C3F786D6C");
        builder.put("zip", "504B0304");
        builder.put("rar", "52617221");
        builder.put("psd", "38425053"); // Photoshop (psd)
        builder.put("eml", "44656C69766572792D646174653A"); // Email [thorough only] (eml)
        builder.put("dbx", "CFAD12FEC5FD746F"); // Outlook Express (dbx)
        builder.put("pst", "2142444E"); // Outlook (pst)
        builder.put("xls", "D0CF11E0"); // MS Word
        builder.put("doc", "D0CF11E0"); // MS Excel 注意：word 和 excel的文件头一样
        builder.put("mdb", "5374616E64617264204A"); // MS Access (mdb)
        builder.put("wpd", "FF575043"); // WordPerfect (wpd)
        builder.put("eps", "252150532D41646F6265");
        builder.put("ps",  "252150532D41646F6265");
        builder.put("pdf", "255044462D312E"); // Adobe Acrobat (pdf)
        builder.put("qdf", "AC9EBD8F"); // Quicken (qdf)
        builder.put("pwl", "E3828596"); // Windows Password (pwl)
        builder.put("wav", "57415645"); // Wave (wav)
        builder.put("avi", "41564920");
        builder.put("ram", "2E7261FD"); // Real Audio (ram)
        builder.put("rm", "2E524D46"); // Real Media (rm)
        builder.put("mpg", "000001BA");
        builder.put("mov", "6D6F6F76"); // Quicktime (mov)
        builder.put("asf", "3026B2758E66CF11"); // Windows Media (asf)
        builder.put("mid", "4D546864"); // MIDI (mid)
        FILE_TYPE_MAGIC = builder.build();
    }

    /**
     * 猜测文件类型
     * @param file
     * @return
     * @throws IOException
     */
    public static String guessFileType(File file) throws IOException {
        try (InputStream input = new FileInputStream(file)) {
            int count = input.available();
            count = count < SUB_PREFIX ? count : SUB_PREFIX;
            byte[] array = new byte[count];
            input.read(array);
            return guessFileType(array);
        }
    }

    public static String guessFileType(byte[] array) {
        if (array.length > SUB_PREFIX) {
            array = ArrayUtils.subarray(array, 0, SUB_PREFIX);
        }

        String hex = Bytes.hexEncode(array).toUpperCase();
        for (Iterator<Entry<String, String>> ite = FILE_TYPE_MAGIC.entrySet().iterator(); ite.hasNext();) {
            Entry<String, String> entry = ite.next();
            if (hex.startsWith(entry.getValue())) {
                return entry.getKey();
            }
        }

        try {
            return net.sf.jmimemagic.Magic.getMagicMatch(array).getExtension();
        } catch (Exception e) {
            return null; // contentType = "text/plain";
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println(guessFileType(new File("d:/代码走查问题表.xlsx")));
    }
}
