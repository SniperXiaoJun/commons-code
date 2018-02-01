package test.jce.ecc0;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

abstract class Chatter {

    Socket socket;
    boolean flag = false;

    public boolean isDecrypt() {
        return flag;
    }

    public OutputStream getOutputStream() throws Exception {
        return socket.getOutputStream();
    }

    public InputStream getInputStream() throws Exception {
        return socket.getInputStream();
    }

    public String readLine() {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            return input.readLine();
        } catch (Exception e) {
            return e.toString();
        }
    }

    public void writeLine(String line) throws Exception {
        try(PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {
            output.println(line);
        }
    }
}
