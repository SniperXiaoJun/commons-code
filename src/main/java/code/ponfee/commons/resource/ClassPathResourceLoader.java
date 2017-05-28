package code.ponfee.commons.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 类资源加载器
 * @author fupf
 */
class ClassPathResourceLoader {

    private static Logger logger = LoggerFactory.getLogger(ClassPathResourceLoader.class);
    private static final String URL_PROTOCOL_FILE = "file";
    private static final String URL_PROTOCOL_JAR = "jar";
    private static final String URL_PROTOCOL_ZIP = "zip";
    private static final String JAR_URL_SEPARATOR = "!/";

    /**
     * 加载资源文件
     * @param filePath
     * @param contextClass
     * @param encoding
     * @return
     */
    Resource getResource(String filePath, Class<?> contextClass, String encoding) {
        Enumeration<URL> urls = null;
        JarFile jar = null;
        ZipFile zip = null;
        try {
            if (contextClass != null) {
                urls = contextClass.getClassLoader().getResources(filePath);
            } else {
                urls = Thread.currentThread().getContextClassLoader().getResources(filePath);
            }

            while (urls.hasMoreElements()) {
                URL url = urls.nextElement(); // 获取下一个元素
                String protocol = url.getProtocol(); // 得到协议的名称
                if ("file".equals(protocol)) {
                    String path = URLDecoder.decode(url.getFile(), encoding);

                    // 判断是否是指定类所在Jar包中的文件
                    if (!checkResourceWithinClass(contextClass, path.substring(0, path.lastIndexOf(filePath)), encoding)) continue;

                    return new Resource(path, new File(path).getName(), new FileInputStream(path));
                } else if ("jar".equals(protocol)) {
                    jar = ((JarURLConnection) url.openConnection()).getJarFile(); // 获取jar

                    // 判断是否是指定类所在Jar包中的文件
                    if (!checkResourceWithinClass(contextClass, jar.getName(), encoding)) continue;

                    // 从此jar包 得到一个枚举类
                    Enumeration<JarEntry> entries = jar.entries();
                    // 同样的进行循环迭代
                    while (entries.hasMoreElements()) {
                        // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                        JarEntry entry = entries.nextElement();

                        if (filePath.equals(entry.getName())) {
                            String fileName = entry.getName();
                            fileName = fileName.replace("\\", "/");
                            if (fileName.indexOf("/") != -1) {
                                fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
                            }
                            return new Resource(URLDecoder.decode(url.getFile(), encoding), fileName, jar.getInputStream(entry));
                        }
                    }
                    jar.close();
                    jar = null;
                } else if ("zip".equals(protocol)) { // weblogic是zip
                    String zipPath = URLDecoder.decode(url.getFile(), encoding);
                    if (zipPath.indexOf(JAR_URL_SEPARATOR) == -1) continue;
                    zipPath = zipPath.substring(0, zipPath.lastIndexOf(JAR_URL_SEPARATOR));

                    // 判断是否是指定类所在Jar包中的文件
                    if (!checkResourceWithinClass(contextClass, zipPath, encoding)) continue;

                    zip = new ZipFile(zipPath);
                    Enumeration<ZipEntry> entries = zip.getEntries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = (ZipEntry) entries.nextElement();

                        if (filePath.equals(entry.getName())) {
                            String fileName = entry.getName();
                            fileName = fileName.replace("\\", "/");
                            if (fileName.indexOf("/") != -1) {
                                fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
                            }

                            return new Resource(URLDecoder.decode(url.getFile(), encoding), fileName, zip.getInputStream(entry));
                        }
                    }
                    zip.close();
                    zip = null;
                }
            }
            return null;
        } catch (IOException e) {
            logger.error("加载Jar包配置文件失败", e);
            return null;
        } finally {
            if (jar != null) try {
                jar.close();
            } catch (IOException e) {
                logger.error("Jar包文件流关闭异常", e);
            }
            if (zip != null) try {
                zip.close();
            } catch (IOException e) {
                logger.error("zip包文件流关闭异常", e);
            }
        }
    }

    List<Resource> listResources(String directory, String[] extensions, boolean recursive, Class<?> contextClass, String encoding) {
        List<Resource> list = new ArrayList<Resource>();
        JarFile jar = null;
        ZipFile zip = null;
        Enumeration<URL> dirs;
        try {
            if (contextClass != null) {
                dirs = contextClass.getClassLoader().getResources(directory);
            } else {
                dirs = Thread.currentThread().getContextClassLoader().getResources(directory);
            }

            // 循环迭代下去
            while (dirs.hasMoreElements()) {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                if (URL_PROTOCOL_FILE.equals(protocol)) {
                    String path = URLDecoder.decode(url.getFile(), encoding);

                    // 判断是否是指定类所在Jar包中的文件
                    if (!checkResourceWithinClass(contextClass, path.substring(0, path.lastIndexOf(directory)), encoding)) continue;

                    Collection<File> files = FileUtils.listFiles(new File(path), extensions, recursive);
                    if (files != null && !files.isEmpty()) {
                        for (File file : files) {
                            list.add(new Resource(file.getAbsolutePath(), file.getName(), new FileInputStream(file)));
                        }
                    }
                } else if (URL_PROTOCOL_JAR.equals(protocol)) {
                    // 读取Jar包
                    jar = ((JarURLConnection) url.openConnection()).getJarFile();

                    // 判断是否是指定类所在Jar包中的文件
                    if (!checkResourceWithinClass(contextClass, jar.getName(), encoding)) continue;

                    // 从此jar包 得到一个枚举类
                    Enumeration<JarEntry> entries = jar.entries();
                    // 同样的进行循环迭代
                    while (entries.hasMoreElements()) {
                        // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                        JarEntry entry = entries.nextElement();
                        if (entry.isDirectory()) continue;

                        String name = entry.getName();

                        /**
                         * <pre>
                         *   1、全目录匹配 或 当可递归时子目录
                         *   2、匹配后缀
                         * </pre>
                         */
                        int idx = name.lastIndexOf('/');
                        boolean isDir = (idx != -1 && name.substring(0, idx).equals(directory)) || (recursive && name.startsWith(directory));
                        boolean isSufx = ArrayUtils.isEmpty(extensions) || name.toLowerCase().matches("^(.+\\.)(" + StringUtils.join(extensions, "|") + ")$");
                        if (isDir && isSufx) {
                            list.add(new Resource(URLDecoder.decode(url.getFile(), encoding), entry.getName(), jar.getInputStream(entry)));
                        }
                    }
                    jar.close();
                } else if (URL_PROTOCOL_ZIP.equals(protocol)) {
                    String zipPath = URLDecoder.decode(url.getFile(), encoding);
                    if (zipPath.indexOf(JAR_URL_SEPARATOR) == -1) continue;
                    zipPath = zipPath.substring(0, zipPath.lastIndexOf(JAR_URL_SEPARATOR));

                    // 判断是否是指定类所在Jar包中的文件
                    if (!checkResourceWithinClass(contextClass, zipPath, encoding)) continue;

                    zip = new ZipFile(zipPath);
                    Enumeration<ZipEntry> entries = zip.getEntries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = (ZipEntry) entries.nextElement();
                        String name = entry.getName();
                        int idx = name.lastIndexOf('/');
                        // 1、全目录匹配 或 当可递归时子目录;2、匹配后缀
                        boolean isDir = (idx != -1 && name.substring(0, idx).equals(directory)) || (recursive && name.startsWith(directory));
                        boolean isSufx = ArrayUtils.isEmpty(extensions) || name.toLowerCase().matches("^(.+\\.)(" + StringUtils.join(extensions, "|") + ")$");
                        if (isDir && isSufx) {
                            list.add(new Resource(URLDecoder.decode(url.getFile(), encoding), entry.getName(), zip.getInputStream(entry)));
                        }
                    }
                    zip.close();
                    zip = null;
                }
            }
        } catch (IOException e) {
            logger.error("配置文件加载失败", e);
        } finally {
            if (jar != null) try {
                jar.close();
            } catch (IOException e) {
                logger.error("jar包文件流关闭异常", e);
            }
            if (zip != null) try {
                zip.close();
            } catch (IOException e) {
                logger.error("zip包文件流关闭异常", e);
            }
        }
        return list;
    }

    /**
     * 判断资源文件是否在contextClass的classpath中（jar包或class目录）
     * @param contextClass
     * @param filepath
     * @param encoding
     * @return
     * @throws IOException
     */
    private static boolean checkResourceWithinClass(Class<?> contextClass, String filepath, String encoding) throws IOException {
        if (contextClass == null) return true;
        String destPath = URLDecoder.decode(contextClass.getProtectionDomain().getCodeSource().getLocation().getFile(), encoding);
        return new File(destPath).getCanonicalFile().equals(new File(filepath).getCanonicalFile());
    }

}
