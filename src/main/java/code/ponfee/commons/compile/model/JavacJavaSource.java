package code.ponfee.commons.compile.model;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.parser.Parser;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;

/**
 * 基于jdk tools jar的语法/词法分析
 * @author fupf
 */
public class JavacJavaSource extends JavaSource {
    private static final long serialVersionUID = 8020419352084840057L;

    public JavacJavaSource(String sourceCode) {
        this.sourceCode = sourceCode;
        new JavaSourceParser().parse(this);
        if (isEmpty(this.publicClass)) {
            throw new IllegalArgumentException("illegal source code, public class not found.");
        }
    }

    /**
     * <pre>
     *  <dependency>
     *    <groupId>jdk-lib</groupId>
     *    <artifactId>tools</artifactId>
     *    <version>1.8.0_40</version>
     *    <scope>system</scope>
     *    <!-- jdk1.8.0_40/lib/tools.jar -->
     *    <systemPath>${project.basedir}/lib/tools.jar</systemPath>
     *  </dependency>
     * </pre>
     * 
     * java源码分析
     */
    private static final class JavaSourceParser {
        private ParserFactory factory;

        JavaSourceParser() {
            Context context = new Context();
            JavacFileManager.preRegister(context);
            factory = ParserFactory.instance(context);

        }

        /*public JavaSource parse(String sourceCode) {
            JavaSource source = new JavaSource(sourceCode);
            this.parse(source);
            return source;
        }*/

        public void parse(JavaSource source) {
            String sourceCode = source.getSourceCode();
            Parser parser = factory.newParser(sourceCode, true, false, true);
            JCCompilationUnit unit = parser.parseCompilationUnit();
            source.packageName = unit.getPackageName().toString();
            new SourceVisitor().visitCompilationUnit(unit, source);
        }
    }

    /**
     * 最外围类（class TD）总在最后
     * 
     * <pre>
     * public final class TD {
     *     public void say() {}
     * 
     *     public class TA {
     *         public String hello() {
     *             return "hello";
     *         }
     *     }
     * 
     *     public class TB {
     *         public String hello() {
     *             return "hello";
     *         }
     *     }
     * 
     *     public class TV {
     *     }
     * 
     *     public class TF {
     *         public String hello() {
     *             return "hello";
     *         }
     *     }
     * 
     *     public class TC {
     *     }
     * }
     * 
     * class D {
     * }
     * </pre>
     */
    private static class SourceVisitor extends TreeScanner<Void, JavaSource> {
        private static final List<Modifier> MODIFIERS = Arrays.asList(PUBLIC, FINAL, ABSTRACT);

        @Override
        public Void visitClass(ClassTree classtree, JavaSource source) {
            super.visitClass(classtree, source);
            Set<Modifier> modifiers = classtree.getModifiers().getFlags();
            // public [final|abstract] class A {}
            if (modifiers.contains(PUBLIC) && modifiers.size() <= 2
                && MODIFIERS.containsAll(modifiers)) {
                source.publicClass = classtree.getSimpleName().toString();
            }
            return null;
        }

    }

    private static CharSequence readFile(String file) {
        FileInputStream in = null;
        FileChannel channel = null;
        try {
            in = new FileInputStream(file);
            channel = in.getChannel();
            ByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, channel.size());
            return Charset.defaultCharset().decode(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (channel != null) try {
                channel.close();
            } catch (IOException e) {
                // ignored
            }

            if (in != null) try {
                in.close();
            } catch (IOException e) {
                // ignored
            }
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        String s = readFile("d:/TT.java").toString();
        System.out.println(s);
        JavaSource source = new JavacJavaSource(s);
        System.out.println(source.getFullyQualifiedName());
    }
}
