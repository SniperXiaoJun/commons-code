package code.ponfee.commons.resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * 资源类
 * @author fupf
 */
public class Resource implements AutoCloseable {

    private final String path;
    private final String fileName;
    private InputStream stream;

    public Resource(String path, String fileName, InputStream stream) {
        this.path = path;
        this.fileName = fileName;
        this.stream = stream;
    }

    public String getPath() {
        return path;
    }

    public String getFileName() {
        return fileName;
    }

    public InputStream getStream() {
        return stream;
    }

    @Override
    public String toString() {
        return "Resource [path=" + path + ", fileName=" + fileName + ", stream=" + stream + "]";
    }

    @Override
    public void close() {
        if (stream != null) try {
            stream.close();
            stream = null;
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
    }

}
