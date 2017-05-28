package code.ponfee.commons.resource;

import java.io.InputStream;

/**
 * 获取资源
 * @author fupf
 */
public class Resource {

    private String path;
    private String fileName;
    private InputStream stream;

    public Resource() {}

    public Resource(String path, String fileName, InputStream stream) {
        super();
        this.path = path;
        this.fileName = fileName;
        this.stream = stream;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public InputStream getStream() {
        return stream;
    }

    public void setStream(InputStream stream) {
        this.stream = stream;
    }
}
