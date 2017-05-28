package code.ponfee.commons.compile;

import code.ponfee.commons.compile.model.JavaSource;

/**
 * 源码编译，更改自alibaba代码
 * @author fupf
 */
public interface JavaSourceCompiler {

    Class<?> compile(String sourceString);

    Class<?> compile(JavaSource javaSource);

}
