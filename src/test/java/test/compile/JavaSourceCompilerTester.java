
package test.compile;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import $._.a.b.n323c23.$._.CompilerSource;
import code.ponfee.commons.compile.impl.JdkCompiler;
import code.ponfee.commons.compile.model.JavaSource;
import code.ponfee.commons.reflect.ClassUtils;
import code.ponfee.commons.util.Streams;

public class JavaSourceCompilerTester {

    @Test
    public void testComplex() throws Exception {
        Class<?> _clazz = CompilerSource.class;
        //Class<?> _clazz = JavaSourceCompilerTest.class;

        String path = ClassUtils.getClasspath(_clazz);
        path = new File(path).getParentFile().getParentFile().getPath() + "/src/test/java/";
        path += _clazz.getCanonicalName().replace('.', '/') + ".java";
        String sourceCode = Streams.file2string(path);
        JavaSource javaSource = new JavaSource(sourceCode);
        System.out.println("packageName:" + javaSource.getPackageName());
        System.out.println("className:" + javaSource.getPublicClass());

        Class<?> clazz = new JdkCompiler().compile(javaSource);

        String s = "_clazz==clazz --> " + (_clazz == clazz) + "  ";
        System.out.println(s + _clazz.getClassLoader().getClass());
        System.out.println(StringUtils.leftPad(" ", s.length()) + clazz.getClassLoader().getClass());

        clazz.getMethod("say").invoke(clazz);
    }
    
    
    public @Test void test() throws Exception {
        String sourceCode = Streams.file2string("d:/String.java");
        JavaSource javaSource = new JavaSource(sourceCode);
        System.out.println(javaSource.getFullyQualifiedName());

        Class<?> clazz = new JdkCompiler().compile(javaSource);
        //clazz.getMethod("sayHello").invoke(clazz);

        Class<?> clazz2 = new JdkCompiler().compile(javaSource);
        System.out.println(clazz==clazz2);
        Class<?> clazz3 = new JdkCompiler().compileForce(javaSource);
        System.out.println(clazz==clazz3);
        Class<?> clazz4 = new JdkCompiler().compileForce(javaSource);
        System.out.println(clazz3==clazz4);
    }
    
}
