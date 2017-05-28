package code.ponfee.commons.compile.model;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则匹配不精确，建议用jdk-tools工具来进行词法分析
 * 只会匹配第一个public class类
 * @author fupf
 */
public class JavaSource implements Serializable {

    private static final long serialVersionUID = -9205215223349262114L;

    /** 正则提取：(?m)启用多行 */
    private static final Pattern PACKAGE_NAME = Pattern.compile("(?m)^\\s*package\\s+([^;]+);");
    private static final Pattern PUBLIC_CLASS = Pattern.compile("(?m)^\\s*public(((\\s+strictfp)?(\\s+(final|abstract))?)|((\\s+(final|abstract))?(\\s+strictfp)?))\\s+class\\s+\\b([a-zA-Z_$][a-zA-Z0-9_$]*)\\b(\\s+extends\\s+\\b([a-zA-Z_$][a-zA-Z0-9_$]*)\\b)?(\\s+implements\\s+\\b([a-zA-Z_$][a-zA-Z0-9_$]*)\\b(\\s*,\\s*\\b([a-zA-Z_$][a-zA-Z0-9_$]*)\\b\\s*)*)?\\s*\\{");

    private final String sourceCode;
    private final String packageName;
    private final String publicClass;

    public JavaSource(String sourceCode) {
        this.sourceCode = sourceCode;

        /*// 通过嵌入式标志表达式 (?s) 也可以启用 dotall 模式（s 是 "single-line" 模式的助记符，在 Perl中也使用它）
        // X*?  X，零次或多次（懒汉模式）
        String findFirst = RegexUtils.findFirst(sourceString, "package (?s).*?;");
        this.packageName = findFirst.replaceAll("package ", EMPTY).replaceAll(";", EMPTY).trim();
        
        findFirst = RegexUtils.findFirst(sourceString, "public class (?s).*?{");
        this.publicClass = findFirst.split("extends")[0].split("implements")[0].replaceAll("public class ", EMPTY).replace("{", EMPTY).trim();*/

        Matcher matcher = PACKAGE_NAME.matcher(sourceCode);
        if (matcher.find()) {
            this.packageName = matcher.group(1).replaceAll("\\s", "");
        } else {
            this.packageName = null;
        }

        matcher = PUBLIC_CLASS.matcher(sourceCode);
        if (matcher.find()) {
            this.publicClass = matcher.group(10);
        } else {
            throw new IllegalArgumentException("invalid java source code, public class not found.");
        }
    }

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

    private static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static void main(String[] args) {
        System.out.println(new JavaSource("public class a {}").getPublicClass());
    }
}
