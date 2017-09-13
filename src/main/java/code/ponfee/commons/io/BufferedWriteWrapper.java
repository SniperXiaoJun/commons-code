package code.ponfee.commons.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * 文件缓冲写入包装类
 * @author Ponfee
 */
public class BufferedWriteWrapper extends Writer {

    private OutputStream outer;
    private OutputStreamWriter writer;
    private BufferedWriter buffer;

    public BufferedWriteWrapper(File file) throws FileNotFoundException {
        this(file, Charset.defaultCharset());
    }

    public BufferedWriteWrapper(File file, Charset charset) throws FileNotFoundException {
        this.outer = new FileOutputStream(file);
        this.writer = new OutputStreamWriter(outer, charset);
        this.buffer = new BufferedWriter(writer, 8192);
    }

    @Override
    public void write(String str) throws IOException {
        buffer.write(str);
    }

    @Override
    public void write(int c) throws IOException {
        buffer.write(c);
    }

    @Override
    public void write(char[] cbuf) throws IOException {
        buffer.write(cbuf);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        buffer.write(str, off, len);
    }

    @Override
    public Writer append(CharSequence csq) throws IOException {
        return buffer.append(csq);
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        return buffer.append(csq, start, end);
    }

    @Override
    public Writer append(char c) throws IOException {
        return buffer.append(c);
    }

    @Override
    public void flush() throws IOException {
        buffer.flush();
        writer.flush();
        outer.flush();
    }

    @Override
    public void close() {
        if (buffer != null) try {
            buffer.close();
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
        buffer = null;

        if (writer != null) try {
            writer.close();
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
        writer = null;

        if (outer != null) try {
            outer.close();
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
        outer = null;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        buffer.write(cbuf, off, len);
    }

    public void newLine() throws IOException {
        buffer.newLine();
    }
}
