package code.ponfee.commons.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

/**
 * 基于zip4j的zip工具类
 * @author fupf
 */
public class ZipUtils {

    private static final String SEPARATOR = "/";
    private static final String SUFFIX = ".zip";

    // -----------------------------------解压缩-----------------------------------
    /**
     * 压缩指定文件到当前文件夹，压缩后的文件名为：待压缩文件名+.zip
     * @param src 要压缩的指定文件
     * @return 最终的压缩文件存放的绝对路径
     */
    public static String zip(String src) throws ZipException {
        return zip(src, null);
    }

    /**
     * 压缩文件到指定路径
     * @param src 待压缩的文件
     * @param dest 压缩文件存放路径，如果没有.zip后缀则会自动加上
     * @return 最终的压缩文件存放的绝对路径
     */
    public static String zip(String src, String dest) throws ZipException {
        return zip(src, dest, null);
    }

    /**
     * 使用给定密码压缩指定文件或文件夹到当前目录
     * @param src 要压缩的文件
     * @param dest 压缩文件存放路径，如果没有.zip后缀则会自动加上
     * @param passwd 压缩使用的密码
     * @return 最终的压缩文件存放的绝对路径
     */
    public static String zip(String src, String dest, String passwd) throws ZipException {
        return zip(src, dest, true, passwd, null);
    }

    /**
     * 使用给定密码压缩指定文件或文件夹到指定位置
     * @param src        待压缩的文件名或文件夹路径名
     * @param dest       压缩文件存放路径：
     *                      如果以路径分隔符“/”结尾则视为目录，压缩文件名取源文件名并加“.zip”后缀<p>
     *                      否则视为文件名<p>
     * @param recursion  是否递归压缩（只对待压缩文件为文件夹时有效）：true是；false否；
     * @param passwd     压缩使用的密码
     * @param comment    注释信息
     * @return 最终的压缩文件存放的绝对路径
     */
    public static String zip(String src, String dest, boolean recursion,
        String passwd, String comment) throws ZipException {
        File srcFile = new File(Strings.cleanPath(src));
        if (!srcFile.exists()) return null;

        dest = buildDestFilePath(srcFile, Strings.cleanPath(dest));
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // 压缩方式
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL); // 压缩级别
        if (!StringUtils.isEmpty(passwd)) {
            parameters.setPassword(passwd.toCharArray());
            parameters.setEncryptFiles(true);
            //parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD); // 加密方式
            parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES); // 加密方式
            parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_128);
        }

        // 开始压缩
        ZipFile zipFile = new ZipFile(dest);
        if (srcFile.isFile()) { // 压缩文件
            zipFile.addFile(srcFile, parameters);
        } else { // 压缩目录
            File[] files = srcFile.listFiles();
            if (files == null || files.length == 0) {
                return null;
            }
            for (File file : files) {
                if (file.isFile()) {
                    zipFile.addFile(file, parameters);
                } else if (recursion) { // 递归压缩目录
                    zipFile.addFolder(file, parameters);
                }
            }
        }

        if (comment != null) {
            zipFile.setComment(comment);
        }
        return dest;
    }

    // -----------------------------------解压缩-----------------------------------
    /**
     * 解压缩文件到当前目录
     * @param zipFile 压缩文件
     * @return 解压后文件数组
     * @throws ZipException
     */
    public static File[] unzip(String zipFile) throws ZipException {
        String dest = zipFile;
        if (dest.toLowerCase().endsWith(SUFFIX)) {
            dest = dest.substring(0, dest.toLowerCase().indexOf(SUFFIX));
        }
        return unzip(zipFile, dest);
    }

    /**
     * 解压缩文件到指定目录
     * @param zipFile 指定的压缩文件
     * @param dest 解压缩存放的目录
     * @return  解压后文件数组
     * @throws ZipException 压缩文件有损坏或者解压缩失败抛出
     */
    public static File[] unzip(String zipFile, String dest) throws ZipException {
        return unzip(zipFile, dest, null);
    }

    /**
     * 使用给定密码解压指定的压缩文件到指定目录<p>
     * 如果指定目录不存在，可以自动创建，不合法的路径将导致异常被抛出
     * @param zip 指定的压缩文件
     * @param dest 解压目录
     * @param passwd 压缩文件的密码
     * @return 解压后文件数组
     * @throws ZipException 压缩文件有损坏或者解压缩失败抛出
     */
    public static File[] unzip(String zipFile, String dest, String passwd) throws ZipException {
        return unzip(new File(zipFile), dest, passwd, "UTF-8");
    }

    /**
     * 使用给定密码解压指定的压缩文件到指定目录<p>
     * 如果指定目录不存在,可以自动创建,不合法的路径将导致异常被抛出
     * @param zipFile 指定的压缩文件
     * @param dest 解压目录
     * @param passwd 压缩文件的密码
     * @param charset 字符编码
     * @return  解压后文件数组
     * @throws ZipException 压缩文件有损坏或者解压缩失败抛出
     */
    @SuppressWarnings("unchecked")
    public static File[] unzip(File zipFile, String dest, String passwd, String charset) throws ZipException {
        if (StringUtils.isEmpty(dest)) {
            throw new IllegalArgumentException("dest path can't be null");
        }
        Files.mkdir(dest); // 校验并创建解压缩存放目录

        ZipFile zFile = new ZipFile(zipFile);
        if (!StringUtils.isEmpty(charset)) {
            zFile.setFileNameCharset(charset);
        }
        if (!zFile.isValidZipFile()) {
            throw new ZipException("invalid zip file.");
        }
        if (zFile.isEncrypted()) {
            if (StringUtils.isEmpty(passwd)) {
                throw new IllegalArgumentException("passwd can't be null");
            } else {
                zFile.setPassword(passwd.toCharArray());
            }
        }
        zFile.extractAll(dest);

        List<FileHeader> headerList = zFile.getFileHeaders();
        List<File> extractedFileList = new ArrayList<File>();
        for (FileHeader fileHeader : headerList) {
            if (!fileHeader.isDirectory()) {
                extractedFileList.add(new File(dest, fileHeader.getFileName()));
            }
        }
        return extractedFileList.toArray(new File[extractedFileList.size()]);
    }

    /**
     * 构建压缩文件存放路径，如果不存在将会创建
     * 传入的可能是文件名或者目录，也可能不传，此方法用以转换最终压缩文件的存放路径
     * @param srcFile 源文件
     * @param destFilePath 压缩目标路径
     * @return 正确的压缩文件存放路径
     */
    private static String buildDestFilePath(File srcFile, String destFilePath) {
        if (StringUtils.isEmpty(destFilePath)) {
            destFilePath = srcFile.getParent() + SEPARATOR; // 为上一级目录
        }

        String parentPath;
        if (destFilePath.endsWith(SEPARATOR)) {
            parentPath = destFilePath; // 路径为目录
            if (srcFile.isDirectory()) {
                destFilePath += srcFile.getName(); // 以目录名作为文件名
            } else {
                destFilePath += FilenameUtils.getBaseName(srcFile.getName()); // 文件名去除后缀
            }
        } else {
            // 获取父路径
            parentPath = destFilePath.substring(0, destFilePath.lastIndexOf(SEPARATOR));
        }

        Files.mkdir(new File(parentPath)); // 创建父路径（如果不存在）

        if (!destFilePath.toLowerCase().endsWith(SUFFIX)) {
            destFilePath += SUFFIX;
        }

        if (new File(destFilePath).exists()) {
            destFilePath = destFilePath.substring(0, destFilePath.lastIndexOf(SUFFIX))
                + "[" + ObjectUtils.uuid(32).toLowerCase() + "]" + SUFFIX;
        }
        return destFilePath;
    }

    public static void main(String[] args) throws Exception {
        //File f = new File("d:/aaa.zip");
        //System.out.println(f.getName()); // aaa.zip
        //System.out.println(FilenameUtils.getBaseName(f.getName())); // aaa

        //zip("D:\\test.txt", "d:\\test.zip");

        zip("D:\\a", null);
        //zip("D:\\guiminer", null, true, null, "abc");
        //unzip("D:\\guiminer.zip");
        //unzip(new File("d:/aaa.zip"), "d://aaa", "123", "UTF-8");
    }
}
