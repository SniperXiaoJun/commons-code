package code.ponfee.commons.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * xml构建
 * @author fupf
 */
public final class XmlWriters {
    private static final String DEFAULT_ROOT = "xml";
    private final List<E> elements = new ArrayList<>();

    private XmlWriters() {}

    public static XmlWriters create() {
        return new XmlWriters();
    }

    public XmlWriters element(String name, String text) {
        E e = new TextE(name, text);
        elements.add(e);
        return this;
    }

    public XmlWriters element(String name, Number number) {
        E e = new NumberE(name, number);
        elements.add(e);
        return this;
    }

    public XmlWriters element(String parentName, String childName, String childText) {
        return element(parentName, new TextE(childName, childText));
    }

    public XmlWriters element(String parentName, String childName, Number childNumber) {
        return element(parentName, new NumberE(childName, childNumber));
    }

    /**
     * 构建包含多个子元素的元素
     * @param parentName 父元素标签名
     * @param childPairs childName1, childValue1, childName2, childValu2, ...，长度必读为2的倍数
     * @return this
     */
    public XmlWriters element(String parentName, Object... childPairs) {
        if (childPairs.length % 2 != 0) {
            throw new XmlException("var args's length must % 2 = 0");
        }
        E parent = new TextE(parentName, null);
        List<E> children = new ArrayList<>();
        E child;
        for (int i = 0; i < childPairs.length; i = i + 2) {
            if (childPairs[i + 1] instanceof Number) {
                child = new NumberE((String) childPairs[i], (Serializable) childPairs[i + 1]);
            } else {
                child = new TextE((String) childPairs[i], (Serializable) childPairs[i + 1]);
            }
            children.add(child);
        }
        parent.children = children;
        elements.add(parent);
        return this;
    }

    public XmlWriters element(String parentName, E child) {
        E e = new TextE(parentName, null);
        e.children = Arrays.asList(child);
        elements.add(e);
        return this;
    }

    public XmlWriters element(String parentName, List<E> children) {
        E e = new TextE(parentName, null);
        e.children = children;
        elements.add(e);
        return this;
    }

    /**
     * 构建包含多个子元素的元素
     * @param parentName 父元素标签名
     * @param childPairs childName1, childValue1, childName2, childValu2, ...，长度必读为2的倍数
     * @return an element
     */
    public E newElement(String parentName, Object... childPairs) {
        E parent = new TextE(parentName, null);
        List<E> children = new ArrayList<>();
        E child;
        for (int i = 0; i < childPairs.length; i = i + 2) {
            if (childPairs[i + 1] instanceof Number) {
                child = new NumberE((String) childPairs[i], (Serializable) childPairs[i + 1]);
            } else {
                child = new TextE((String) childPairs[i], (Serializable) childPairs[i + 1]);
            }
            children.add(child);
        }
        parent.children = children;
        return parent;
    }

    public String build() {
        return build(DEFAULT_ROOT);
    }

    public String build(String root) {
        StringBuilder xml = new StringBuilder();
        xml.append("<").append(root).append(">");

        if (elements != null && elements.size() > 0) {
            for (E e : elements) {
                xml.append(e.render());
            }
        }
        xml.append("</").append(root).append(">");
        return xml.toString();
    }

    private abstract static class E {
        protected String name;
        protected Object text;
        protected List<E> children;

        protected E(String name, Object text) {
            this.name = name;
            this.text = text;
        }

        protected abstract String render();
    }

    private static class TextE extends E {
        TextE(String name, Serializable content) {
            super(name, content);
        }

        @Override
        protected String render() {
            StringBuilder content = new StringBuilder();
            content.append("<").append(name).append(">");

            if (text != null) {
                content.append("<![CDATA[").append(text).append("]]>");
            }

            if (children != null && children.size() > 0) {
                for (E child : children) {
                    content.append(child.render());
                }
            }

            content.append("</").append(name).append(">");
            return content.toString();
        }
    }

    private static class NumberE extends E {
        NumberE(String name, Serializable content) {
            super(name, content);
        }

        @Override
        protected String render() {
            StringBuilder content = new StringBuilder();
            content.append("<").append(name).append(">").append(text).append("</").append(name).append(">");
            return content.toString();
        }
    }
}
