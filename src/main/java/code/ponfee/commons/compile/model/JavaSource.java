package code.ponfee.commons.compile.model;

import java.io.Serializable;

/**
 * java源代码类
 * @author fupf
 */
public abstract class JavaSource implements Serializable {

    private static final long serialVersionUID = 5643697448853377651L;

    protected String sourceCode; // 源码
    protected String packageName; // 包名
    protected String publicClass; // public修饰的类名

    public String getSourceCode() {
        return sourceCode;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getPublicClass() {
        return publicClass;
    }

    /**
     * 获取类的全限定名
     * @return
     */
    public String getFullyQualifiedName() {
        return (isEmpty(packageName) ? "" : packageName + ".") + publicClass;
    }

    static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

}
