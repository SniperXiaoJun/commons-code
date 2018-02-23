package code.ponfee.commons.reflect;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.ClassWriter;
import org.springframework.asm.Label;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.asm.Type;

import code.ponfee.commons.io.Files;
import code.ponfee.commons.util.ObjectUtils;

/**
 * 基于asm的字节码工具类
 * @author fupf
 */
public final class ClassUtils {

    private ClassUtils() {}

    /**
     * 获取方法的参数名（编译未清除）<p>
     * getMethodParamNames()                         -> []
     * getMethodSignature(Method method)             -> [method]
     * getClassGenricType(Class<?> clazz, int index) -> [clazz,index]
     * @param method
     * @return
     */
    public static String[] getMethodParamNames(final Method method) {
        ClassReader classReader = null;
        try {
            // 第一种方式，cannot use in jar file
            /*String name = getClassFilePath(method.getDeclaringClass());
            classReader = new ClassReader(new FileInputStream(name));*/

            // 第二种方式（sometimes was wrong）
            //classReader = new ClassReader(getClassName(method.getDeclaringClass()));

            // 第三种方式
            Class<?> clazz = method.getDeclaringClass();
            classReader = new ClassReader(clazz.getResourceAsStream(clazz.getSimpleName() + ".class"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final String[] paramNames = new String[method.getParameterTypes().length];
        classReader.accept(new ClassVisitor(Opcodes.ASM5, new ClassWriter(ClassWriter.COMPUTE_MAXS)) {
            public @Override MethodVisitor visitMethod(int access, String name, String desc, String sign, String[] ex) {
                if (!name.equals(method.getName()) || !sameType(Type.getArgumentTypes(desc), method.getParameterTypes())) {
                    return super.visitMethod(access, name, desc, sign, ex); // 方法名相同并且参数个数相同
                }

                return new MethodVisitor(Opcodes.ASM5, cv.visitMethod(access, name, desc, sign, ex)) {
                    public @Override void visitLocalVariable(String name, String desc, String sign, Label start, Label end, int index) {
                        int i = index;
                        if (!Modifier.isStatic(method.getModifiers())) {
                            i -= 1; // 非静态方法第一个参数是“this”
                        }
                        if (i >= 0 && i < paramNames.length) {
                            paramNames[i] = name;
                        }
                        super.visitLocalVariable(name, desc, sign, start, end, index);
                    }
                };
            }
        }, 0);

        return paramNames;
    }

    /**
     * 获取方法签名
     * Method m = ObjectUtils.class.getMethod("map2array", List.class, String[].class);
     * getMethodSignature(m) -> public static java.util.List code.ponfee.commons.util.ObjectUtils.map2array(java.util.List data, java.lang.String[] fields)
     * @param method
     * @return the method string
     * @see java.lang.reflect.Method#toString()
     * @see java.lang.reflect.Method#toGenericString()
     */
    public static String getMethodSignature(final Method method) {
        String[] names = getMethodParamNames(method);
        Class<?>[] types = method.getParameterTypes();

        List<String> params = new ArrayList<>();
        for (int i = 0; i < types.length; i++) {
            params.add(getClassName(types[i]) + " " + names[i]);
        }

        return new StringBuilder(Modifier.toString(method.getModifiers() & Modifier.methodModifiers()))
                    .append(' ').append(getClassName(method.getReturnType()))
                    .append(' ').append(getClassName(method.getDeclaringClass()))
                    .append('.').append(method.getName())
                    .append('(').append(StringUtils.join(params.toArray(), ", ")).append(')')
                    .toString();
    }

    /**
     * 获取反射字段对象
     * @param clazz
     * @param field
     * @return Filed object
     * @throws Exception
     */
    public static Field getField(Class<?> clazz, String field) throws Exception {
        if (clazz.isInterface() || clazz == Object.class) {
            return null;
        }

        Exception ex;
        do {
            try {
                return clazz.getDeclaredField(field);
            } catch (NoSuchFieldException | SecurityException e) {
                ex = e;
                clazz = clazz.getSuperclass();
            }
        } while (clazz != null && clazz != Object.class);

        throw ex;
    }

    /**
     * 获取类名称
     * getClassName(ClassUtils.class)  ->  code.ponfee.commons.reflect.ClassUtils
     * @param clazz
     * @return
     */
    public static String getClassName(Class<?> clazz) {
        String name = clazz.getCanonicalName();
        if (name == null) {
            name = clazz.getName();
        }

        return name;
    }

    /**
     * 包名称转目录路径名
     * getPackagePath("code.ponfee.commons.reflect")  ->  code/ponfee/commons/reflect
     * @see org.springframework.util.ClassUtils#convertClassNameToResourcePath
     * @param className
     * @return
     */
    public static String getPackagePath(String packageName) {
        return packageName.replace('.', '/');
    }

    /**
     * 包名称转目录路径名
     * getPackagePath(ClassUtils.class)  ->  code/ponfee/commons/reflect
     * @param clazz
     * @return
     */
    public static String getPackagePath(Class<?> clazz) {
        String className = getClassName(clazz);
        if (className.indexOf('.') < 0) {
            return ""; // none package name
        }

        return getPackagePath(className.substring(0, className.lastIndexOf('.')));
    }

    /**
     * 获取类文件的路径（文件）
     * getClassFilePath(ClassUtils.class)  ->  D:\github\commons-code\target\classes\code\ponfee\commons\reflect\ClassUtils.class
     * getClassFilePath(StringUtils.class) ->  D:\maven_repos\org\apache\commons\commons-lang3\3.5\commons-lang3-3.5.jar!\org\apache\commons\lang3\StringUtils.class
     * @param clazz
     * @return
     */
    public static String getClassFilePath(Class<?> clazz) {
        URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
        String path = null;
        try {
            path = URLDecoder.decode(url.getPath(), Files.UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        path = new File(path).getAbsolutePath();

        if (path.toLowerCase().endsWith(".jar")) {
            path += "!";
        }
        return path + File.separator + getClassName(clazz).replace('.', File.separatorChar) + ".class";
    }

    /**
     * 获取指定类的类路径（目录）
     * getClasspath(ClassUtils.class)   ->  D:\github\commons-code\target\classes\
     * getClasspath(StringUtils.class)  ->  D:\maven_repos\org\apache\commons\commons-lang3\3.5\
     * @param clazz
     * @return
     */
    public static String getClasspath(Class<?> clazz) {
        URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
        String path = null;
        try {
            path = URLDecoder.decode(url.getPath(), Files.UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        if (path.toLowerCase().endsWith(".jar")) {
            path = path.substring(0, path.lastIndexOf("/") + 1);
        }
        return new File(path).getAbsolutePath() + File.separator;
    }

    /**
     * 获取当前的类路径（目录）
     * getClasspath()  ->  D:\github\commons-code\target\classes\
     * @return
     */
    public static String getClasspath() {
        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        try {
            path = URLDecoder.decode(new File(path).getAbsolutePath(), Files.UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return path + File.separator;
    }

    /**
     * 比较参数类型是否一致<p>
     * @param types asm的类型({@link Type})
     * @param clazzes java 类型({@link Class})
     * @return {@code true} if the Type array each of equals the Class array
     */
    private static boolean sameType(Type[] types, Class<?>[] clazzes) {
        if (types.length != clazzes.length) {
            return false;
        }

        for (int i = 0; i < types.length; i++) {
            if (!Type.getType(clazzes[i]).equals(types[i])) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(getClassName(ClassUtils.class));
        System.out.println(getPackagePath("code.ponfee.commons.reflect"));
        System.out.println(getPackagePath(ClassUtils.class));
        //Method m = ObjectUtils.class.getMethod("shortid", int.class, char[].class);
        Method m = ObjectUtils.class.getMethod("map2bean", Map.class, Class.class);
        //Method m = ObjectUtils.class.getMethod("uuid32");
        System.out.println(getMethodParamNames(m) == null);
        System.out.println(StringUtils.join(getMethodParamNames(m), ","));
        System.out.println("getMethodSignature: " + getMethodSignature(m));
        System.out.println("m.toString: " + m.toString());
        System.out.println("toGenericString: " + m.toGenericString());

        System.out.println("========================================");
        System.out.println(getClasspath(ClassUtils.class));
        System.out.println(getClasspath());
        System.out.println(getClasspath(StringUtils.class));

        System.out.println("========================================");
        System.out.println(getClassFilePath(ClassUtils.class));
        System.out.println(getClassFilePath(StringUtils.class));
    }

}
