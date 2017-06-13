package code.ponfee.commons.util;

import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.text.DecimalFormat;

import org.apache.commons.lang3.StringUtils;

import sun.security.action.GetPropertyAction;

@SuppressWarnings("restriction")
public class Files {

    private static final String[] FILE_SIZE_UNITS = { "B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB" };

    public static final String LINE_SEPARATOR;
    static {
        String separator = (String) AccessController.doPrivileged(new GetPropertyAction("line.separator"));
        if (StringUtils.isEmpty(separator)) separator = System.getProperty("line.separator", "/n");
        LINE_SEPARATOR = separator;
    }

    /** 
     * 获取带单位的文件名，单位会自动显示为合适的值，如B、KB、MB等 
     * @param size 文件字节大小 
     */
    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        int digit = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digit)) + FILE_SIZE_UNITS[digit];
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
            throw new IllegalArgumentException("file [" + file.getAbsolutePath() + "] not a dir");
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
                throw new RuntimeException(e);
            }
        } else if (file.isDirectory()) {
            throw new IllegalArgumentException("dir [" + file.getAbsolutePath() + "] not a file");
        }
        return file;
    }

    public static void main(String[] args) {
        System.out.println(readableFileSize(1456125000l));
        System.out.println(LINE_SEPARATOR);
        touch(new File("D:\\test"));
    }
}
