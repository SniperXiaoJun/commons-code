package code.ponfee.commons.compile.model;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

import java.io.FileNotFoundException;
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

import code.ponfee.commons.io.Files;

/**
 * <pre>
 *    <dependency>
 *      <groupId>jdk-lib</groupId>
 *      <artifactId>tools</artifactId>
 *      <version>1.8.0_121</version>
 *      <!-- <scope>system</scope>
 *      <systemPath>${pom.basedir}/lib/jdk-tools-1.8.0_121.jar</systemPath> -->
 *    </dependency>
 * </pre>
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
     * java源码解析类
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
            Parser parser = factory.newParser(source.getSourceCode(), true, false, true);
            JCCompilationUnit unit = parser.parseCompilationUnit();
            source.packageName = unit.getPackageName().toString();
            source.publicClass = new SourceVisitor().visitCompilationUnit(unit, source);
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
     * 源码访问类
     */
    private static class SourceVisitor extends TreeScanner<String, JavaSource> {
        private static final List<Modifier> MODIFIERS = Arrays.asList(PUBLIC, FINAL, ABSTRACT);

        @Override
        public String visitClass(ClassTree classtree, JavaSource source) {
            super.visitClass(classtree, source);
            Set<Modifier> modifiers = classtree.getModifiers().getFlags();
            // public [final|abstract] class A {}
            if (modifiers.contains(PUBLIC) && modifiers.size() <= 2
                && MODIFIERS.containsAll(modifiers)) {
                return classtree.getSimpleName().toString();
            } else {
                return null;
            }
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        String s = Files.toString("d:/TT.java");
        System.out.println(s);
        JavaSource source = new JavacJavaSource(s);
        System.out.println(source.getFullyQualifiedName());
    }
}
