package code.ponfee.commons.util;

import java.io.File;

/**
 * maven标准的项目文件工具类
 * @author fupf
 */
public class MavenProjects {

    private static String getProjectBaseDir() {
        String path = Thread.currentThread().getContextClassLoader().getResource("").getFile();
        return new File(path).getParentFile().getParentFile().getPath();
    }

    public static File getMainJavaFile(Class<?> clazz) {
        String path = getProjectBaseDir() + "/src/main/java/";
        path += clazz.getCanonicalName().replace('.', '/') + ".java";
        return new File(path);
    }

    public static File getTestJavaFile(Class<?> clazz) {
        String path = getProjectBaseDir() + "/src/test/java/";
        path += clazz.getCanonicalName().replace('.', '/') + ".java";
        return new File(path);
    }

    public static String getMainJavaPath(String basePackage) {
        return getProjectBaseDir() + "/src/main/java/" + basePackage.replace('.', '/');
    }

    public static String getMainJavaPath(String basePackage, String filename) {
        return getMainJavaPath(basePackage) + "/" + filename;
    }

    public static String getTestJavaPath(String basePackage) {
        return getProjectBaseDir() + "/src/test/java/" + basePackage.replace('.', '/');
    }

    public static String getTestJavaPath(String basePackage, String filename) {
        return getTestJavaPath(basePackage) + "/" + filename;
    }

    public static String getMainResourcesPath() {
        return getProjectBaseDir() + "/src/main/resources/";
    }

    public static String getMainResourcesPath(String contextPath) {
        return getMainResourcesPath() + contextPath;
    }

    public static String getTestResourcesPath() {
        return getProjectBaseDir() + "/src/test/resources/";
    }

    public static String getTestResourcesPath(String contextPath) {
        return getTestResourcesPath() + contextPath;
    }

    public static String getWebAppPath() {
        return getProjectBaseDir() + "/src/main/webapp/";
    }

    public static String getWebAppPath(String webappPath) {
        return getWebAppPath() + webappPath;
    }

}
