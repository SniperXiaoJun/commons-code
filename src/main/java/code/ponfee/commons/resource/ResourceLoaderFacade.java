package code.ponfee.commons.resource;

import java.util.List;

import javax.servlet.ServletContext;

import code.ponfee.commons.reflect.ClassUtils;
import code.ponfee.commons.util.Strings;

/**
 * <pre>
 *   <ul>
 *     <li>
 *      <span>classpath:</span>
 *        filePath默认为""<p>
 *        当以“/”开头时contextClass只起到jar包定位的作用<p>
 *        不以“/”开头时contextClass还起到package相对路径的作用<p>
 *     </li>
 *     <li>webapp:</li>
 *     <li>file:</li>
 *   </ul>
 *   <p>default classpath:</p>
 * </pre>
 * 
 * 资源文件加载门面类
 * @author fupf
 */
public class ResourceLoaderFacade {

    private static final String CP_PREFIX = "classpath:";
    private static final String WEB_PREFIX = "webapp:";
    private static final String FS_PREFIX = "file:";
    private static final String DEFAULT_ENCODING = "UTF-8";
    //private static final String CP_ALL_PREFIX = "classpath*:";

    private static final ClassPathResourceLoader CP_LOADER = new ClassPathResourceLoader();
    private static final FileSystemResourceLoader FS_LOADER = new FileSystemResourceLoader();
    private static final ServletContextResourceLoader WEB_LOADER = new ServletContextResourceLoader();

    public static void setServletContext(ServletContext servletContext) {
        WEB_LOADER.setServletContext(servletContext);
    }

    public static Resource getResource(String filePath, Class<?> contextClass) {
        return getResource(filePath, contextClass, null);
    }

    public static Resource getResource(String filePath, String encoding) {
        return getResource(filePath, null, encoding);
    }

    public static Resource getResource(String filePath) {
        return getResource(filePath, null, null);
    }

    /**
     * 文件资源加载
     * @param filePath        "/"表示根路径开始，其它为相对路径
     * @param contextClass
     * @param encoding
     * @return
     */
    public static Resource getResource(String filePath, Class<?> contextClass, String encoding) {
        if (encoding == null || encoding.length() == 0) {
            encoding = DEFAULT_ENCODING;
        }
        if (filePath == null) filePath = "";
        String path = cleanPath(filePath);
        if (filePath.startsWith(FS_PREFIX)) {
            return FS_LOADER.getResource(path, encoding);
        } else if (filePath.startsWith(WEB_PREFIX)) {
            return WEB_LOADER.getResource(path, encoding);
        } else {
            // 内部用的classLoader加载，不能以“/”开头，XX.class.getResourceAsStream("/com/x/file/myfile.xml")才能以“/”开头
            path = resolveClasspath(path, contextClass);
            return CP_LOADER.getResource(path, contextClass, encoding); // 默认为classpath
        }
    }

    public static List<Resource> listResources(String extensions[], Class<?> contextClass) {
        return listResources("", extensions, false, contextClass, DEFAULT_ENCODING);
    }

    public static List<Resource> listResources(String dir, String extensions[], boolean recursive) {
        return listResources(dir, extensions, recursive, null, null);
    }

    public static List<Resource> listResources(String dir, String extensions[], boolean recursive, String encoding) {
        return listResources(dir, extensions, recursive, null, encoding);
    }

    /**
     * 路径匹配过滤加载
     * @param dir             "/"表示根路径开始，其它为相对路径
     * @param extensions
     * @param recursive
     * @param contextClass
     * @param encoding
     * @return
     */
    public static List<Resource> listResources(String dir, String extensions[], boolean recursive, Class<?> contextClass, String encoding) {
        if (dir == null) dir = "";
        String path = cleanPath(dir);
        if (dir.startsWith(FS_PREFIX)) {
            return FS_LOADER.listResources(path, extensions, recursive);
        } else if (dir.startsWith(WEB_PREFIX)) {
            return WEB_LOADER.listResources(path, extensions, recursive, encoding);
        } else {
            // 内部用的classLoader加载，不能以“/”开头，XX.class.getResourceAsStream("/com/x/file/myfile.xml")才能以“/”开头
            path = resolveClasspath(path, contextClass);
            return CP_LOADER.listResources(path, extensions, recursive, contextClass, encoding); // default classpath
        }
    }

    /*public static File resolveDir(String dir) {
        if (dir == null) dir = "";
        String tmp = cleanPath(dir);
        if (dir.startsWith(WEB_PREFIX)) {
            dir = WEB_LOADER.getServletContext().getRealPath(tmp);
        } else if (dir.startsWith(CP_PREFIX)) {
            String basic = Thread.currentThread().getContextClassLoader().getResource("").getPath();
            dir = Urls.decodeURI(basic, DEFAULT_ENCODING) + tmp;
        } else {
            dir = tmp;
        }
        return Streams.makeDir(dir);
    }*/

    private static String resolveClasspath(String path, Class<?> contextClass) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        } else if (contextClass != null) {
            path = ClassUtils.toResourcePath(contextClass) + "/" + path;
        }
        return path;
    }

    private static String cleanPath(String path) {
        if (path.startsWith(WEB_PREFIX)) {
            path = Strings.cleanPath(path.substring(WEB_PREFIX.length()));
            if (!path.startsWith("/")) {
                path = (new StringBuilder()).append("/").append(path).toString();
            }
        } else if (path.startsWith(FS_PREFIX)) {
            path = Strings.cleanPath(path.substring(FS_PREFIX.length()));
        } else if (path.startsWith(CP_PREFIX)) {
            path = Strings.cleanPath(path.substring(CP_PREFIX.length()));
            if (path.startsWith("/")) path = path.substring(1);
        } else {
            path = Strings.cleanPath(path);
        }
        return path;
    }

}
