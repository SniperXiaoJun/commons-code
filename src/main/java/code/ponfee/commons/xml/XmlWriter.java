package code.ponfee.commons.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * xml构建
 * @author fupf
 */
public final class XmlWriter {
    private final List<E<?>> elements = new ArrayList<>();

    private XmlWriter() {}

    public static XmlWriter create() {
        return new XmlWriter();
    }

    public XmlWriter element(String name, String text) {
        elements.add(new TextE(name, text));
        return this;
    }

    public XmlWriter element(String name, Number number) {
        elements.add(new NumberE(name, number));
        return this;
    }

    public XmlWriter element(String parentName, String childName, String childText) {
        return element(parentName, new TextE(childName, childText));
    }

    public XmlWriter element(String parentName, String childName, Number childNumber) {
        return element(parentName, new NumberE(childName, childNumber));
    }

    public XmlWriter element(String parentName, E<?> child) {
        return element(parentName, Arrays.asList(child));
    }

    public XmlWriter element(String parentName, List<E<?>> children) {
        elements.add(new ComplexE(parentName, children));
        return this;
    }

    /**
     * 构建包含多个子元素的元素
     * @param parentName 父元素名
     * @param childPairs childName1, childValue1, childName2, childValu2, ...，长度必须为2的倍数
     * @return this
     */
    public XmlWriter element(String parentName, Object... childPairs) {
        elements.add(newElement(parentName, childPairs));
        return this;
    }

    /**
     * 构建包含多个子元素的元素
     * @param parentName 父元素标签名
     * @param childPairs childName1, childValue1, childName2, childValu2, ... 长度必须为2的倍数
     * @return an element
     */
    public static E<?> newElement(String parentName, Object... childPairs) {
        if (childPairs.length % 2 != 0) {
            throw new XmlException("args Object array must be pair");
        }

        List<E<?>> children = new ArrayList<>();
        E<?> child;
        for (int i = 0; i < childPairs.length; i = i + 2) {
            if (childPairs[i + 1] instanceof Number) {
                child = new NumberE((String) childPairs[i], (Number) childPairs[i + 1]);
            } else if (childPairs[i + 1] instanceof List<?>) {
                child = new ComplexE((String) childPairs[i], (List<?>) childPairs[i + 1]);
            } else {
                child = new TextE((String) childPairs[i], Objects.toString(childPairs[i + 1], null));
            }
            children.add(child);
        }
        return new ComplexE(parentName, children);
    }

    public String build() {
        return build("xml");
    }

    public String build(String root) {
        StringBuilder xml = new StringBuilder("<").append(root).append(">");
        for (E<?> e : elements) {
            xml.append(e.render());
        }
        return xml.append("</").append(root).append(">").toString();
    }

    private abstract static class E<T> {
        protected final String name;
        protected final T value;

        protected E(String name, T value) {
            if (name == null) {
                throw new IllegalArgumentException("element name cannot be null.");
            }
            this.name = name;
            this.value = value;
        }

        protected abstract String render();
    }

    private static class TextE extends E<String> {
        TextE(String name, String content) {
            super(name, content);
        }

        @Override
        protected String render() {
            StringBuilder content = new StringBuilder("<").append(name).append(">");
            if (value != null) {
                content.append("<![CDATA[").append(value).append("]]>");
            }
            return content.append("</").append(name).append(">").toString();
        }
    }

    private static class NumberE extends E<Number> {
        NumberE(String name, Number value) {
            super(name, value);
        }

        @Override
        protected String render() {
            return new StringBuilder("<").append(name).append(">")
              .append(value).append("</").append(name).append(">")
              .toString();
        }
    }

    private static class ComplexE extends E<List<?>> {
        ComplexE(String name, List<?> nodes) {
            super(name, nodes);
        }

        @Override
        protected String render() {
            StringBuilder content = new StringBuilder("<").append(name).append(">");
            for (Object obj : (List<?>) value) {
                if (obj == null) {
                    continue;
                } else if (obj instanceof E<?>) {
                    content.append(((E<?>) obj).render());
                } else {
                    content.append(obj.toString());
                }
            }
            return content.append("</").append(name).append(">").toString();
        }
    }

    public static void main(String[] args) {
        XmlWriter writers = XmlWriter.create();
        writers.element("k", "v");
        writers.element("book", "price", 98.8, "name", "one book");
        String xml = writers.build("root");
        System.out.println(xml);
    }
}
