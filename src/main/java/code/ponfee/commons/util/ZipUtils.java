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

public class ZipUtils {

    private static final String SEPARATOR = "/";

    /**
     * 压缩指定文件到当前文件夹
     * @param src 要压缩的指定文件
     * @return 最终的压缩文件存放的绝对路径,如果为null则说明压缩失败.
     */
    public static String zip(String src) throws ZipException {
        return zip(src, null);
    }

    /**
     * 使用给定密码压缩指定文件或文件夹到当前目录
     * @param src 要压缩的文件
     * @param passwd 压缩使用的密码
     * @return 最终的压缩文件存放的绝对路径,如果为null则说明压缩失败.
     */
    public static String zip(String src, String passwd) throws ZipException {
        return zip(src, null, passwd);
    }

    /**
     * 使用给定密码压缩指定文件或文件夹到当前目录
     * @param src 要压缩的文件
     * @param dest 压缩文件存放路径
     * @param passwd 压缩使用的密码
     * @return 最终的压缩文件存放的绝对路径,如果为null则说明压缩失败.
     */
    public static String zip(String src, String dest, String passwd) throws ZipException {
        return zip(src, dest, true, passwd, null);
    }

    /**
     * 使用给定密码压缩指定文件或文件夹到指定位置.
     * <p>
     * dest可传最终压缩文件存放的绝对路径,也可以传存放目录,也可以传null或者"".<br />
     * 如果传null或者""则将压缩文件存放在当前目录,即跟源文件同目录,压缩文件名取源文件名,以.zip为后缀;<br />
     * 如果以路径分隔符“/”结尾,则视为目录,压缩文件名取源文件名,以.zip为后缀,否则视为文件名.
     * @param src 要压缩的文件或文件夹路径
     * @param dest 压缩文件存放路径
     * @param recursion 是否递归压缩：true是；false否；
     * @param passwd 压缩使用的密码
     * @param comment 注释信息
     * @return 最终的压缩文件存放的绝对路径
     */
    public static String zip(String src, String dest, boolean recursion, String passwd, String comment) throws ZipException {
        File srcFile = new File(ObjectUtils.cleanPath(src));
        dest = buildDestFilePath(srcFile, ObjectUtils.cleanPath(dest));
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // 压缩方式
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL); // 压缩级别
        if (!StringUtils.isEmpty(passwd)) {
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD); // 加密方式
            parameters.setPassword(passwd.toCharArray());
        }
        ZipFile zipFile = new ZipFile(dest);
        if (!srcFile.isDirectory()) { // 压缩单个文件
            zipFile.addFile(srcFile, parameters);
        } else if (recursion) { // 递归压缩目录
            zipFile.addFolder(srcFile, parameters);
        } else { // 递归压缩目录
            ArrayList<File> files = new ArrayList<File>();
            for (File file : srcFile.listFiles()) {
                if (!file.isDirectory()) {
                    files.add(file);
                }
            }
            zipFile.addFiles(files, parameters);
        }
        if (comment != null) {
            zipFile.setComment(comment);
        }
        return dest;
    }

    /**
     * 使用给定密码解压指定的ZIP压缩文件到指定目录
     * <p>
     * 如果指定目录不存在,可以自动创建,不合法的路径将导致异常被抛出
     * @param zip 指定的ZIP压缩文件
     * @param dest 解压目录
     * @param passwd ZIP文件的密码
     * @return 解压后文件数组
     * @throws ZipException 压缩文件有损坏或者解压缩失败抛出
     */
    public static File[] unzip(String zip, String dest, String passwd) throws ZipException {
        File zipFile = new File(zip);
        return unzip(zipFile, dest, passwd);
    }

    /**
     * 使用给定密码解压指定的ZIP压缩文件到当前目录
     * @param zip 指定的ZIP压缩文件
     * @param passwd ZIP文件的密码
     * @return  解压后文件数组
     * @throws ZipException 压缩文件有损坏或者解压缩失败抛出
     */
    public static File[] unzip(String zip, String passwd) throws ZipException {
        File zipFile = new File(zip);
        File parentDir = zipFile.getParentFile();
        return unzip(zipFile, parentDir.getAbsolutePath(), passwd);
    }

    public static File[] unzip(File zipFile, String dest, String passwd) throws ZipException {
        return unzip(zipFile, dest, passwd, "UTF-8");
    }

    /**
     * 使用给定密码解压指定的ZIP压缩文件到指定目录
     * <p>
     * 如果指定目录不存在,可以自动创建,不合法的路径将导致异常被抛出
     * @param zipFile 指定的ZIP压缩文件
     * @param dest 解压目录
     * @param passwd ZIP文件的密码
     * @param charset 字符编码
     * @return  解压后文件数组
     * @throws ZipException 压缩文件有损坏或者解压缩失败抛出
     */
    @SuppressWarnings("unchecked")
    public static File[] unzip(File zipFile, String dest, String passwd, String charset) throws ZipException {
        ZipFile zFile = new ZipFile(zipFile);
        zFile.setFileNameCharset(charset);
        if (!zFile.isValidZipFile()) {
            throw new ZipException("invalid zip file.");
        }
        File destDir = new File(dest);
        if (destDir.isDirectory() && !destDir.exists()) {
            destDir.mkdir();
        }
        if (zFile.isEncrypted()) {
            zFile.setPassword(passwd.toCharArray());
        }
        zFile.extractAll(dest);

        List<FileHeader> headerList = zFile.getFileHeaders();
        List<File> extractedFileList = new ArrayList<File>();
        for (FileHeader fileHeader : headerList) {
            if (!fileHeader.isDirectory()) {
                extractedFileList.add(new File(destDir, fileHeader.getFileName()));
            }
        }
        return extractedFileList.toArray(new File[extractedFileList.size()]);
    }

    /**
     * 构建压缩文件存放路径,如果不存在将会创建
     * 传入的可能是文件名或者目录,也可能不传,此方法用以转换最终压缩文件的存放路径
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

        if (!destFilePath.toLowerCase().endsWith(".zip")) {
            destFilePath += ".zip";
        }

        return destFilePath;
    }

    public static void main(String[] args) throws ZipException {
        File f = new File("d:/aaa.zip");
        System.out.println(f.getName()); // aaa.zip
        System.out.println(FilenameUtils.getBaseName(f.getName())); // aaa

        System.out.println(zip("d:/hsqldb", "d:/aaab", true, "123", "abc"));
        //unzip(new File("d:/aaa.zip"), "d://aaa", "123", "UTF-8");
    }
}
