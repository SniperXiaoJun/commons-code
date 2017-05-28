package code.ponfee.commons.http;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JavaType;

import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.util.Bytes;
import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicMatchNotFoundException;

/**
 * <pre>
 *  Accept属于请求头， Content-Type属于实体头
 *     请求方的http报头结构：通用报头|请求报头|实体报头 
 *     响应方的http报头结构：通用报头|响应报头|实体报头
 *  
 *  Accept代表请求端（客户端）希望接受的数据类型
 *     比如：Accept：text/xml; 
 *     代表客户端希望接受的数据类型是xml类型
 *  
 *  Content-Type代表实体报头数据类型
 *     比如：Content-Type：text/html; 
 *     代表实体报头的数据格式是html
 * </pre>
 * 
 * http工具类
 * @author fupf
 */
public final class Http {

    private final String url; // url
    private HttpMethod method = HttpMethod.GET; // 请求方法
    private Map<String, String> headers = new HashMap<>(); // http头
    private Map<String, ?> params = Collections.emptyMap(); // http参数
    private String data; // 请求data
    private int connectTimeout = 1000 * 5; // 连接超时时间
    private int readTimeout = 1000 * 5; // 读取返回数据超时时间
    private Boolean encode = Boolean.TRUE; // 是否编码
    private String contentType; // 请求内容类型
    private String contentCharset; // 请求内容编码
    private String accept; // 接收类型
    private List<MimePart> parts = new ArrayList<>(); // 上传的文件
    private SSLSocketFactory sslSocketFactory; // 信任的SSL

    private Http(String url) {
        this.url = url;
    }

    private Http method(HttpMethod method) {
        this.method = method;
        return this;
    }

    // ----------------------------method--------------------------
    public static Http get(String url) {
        return new Http(url);
    }

    public static Http post(String url) {
        return new Http(url).method(HttpMethod.POST);
    }

    public static Http put(String url) {
        return new Http(url).method(HttpMethod.PUT);
    }

    public static Http delete(String url) {
        return new Http(url).method(HttpMethod.DELETE);
    }

    // ----------------------------header--------------------------
    /**
     * 设置请求头
     * @param name
     * @param value
     * @return
     */
    public Http header(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    /**
     * 设置请求头
     * @param headers
     * @return
     */
    public Http headers(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }

    // ----------------------------params--------------------------
    /**
     * 设置请求参数：经使用有点问题，慎用
     * 最终是拼接成queryString的形式追加到url（即作为get的http请求参数）
     * @param params
     * @return
     */
    public Http params(Map<String, ?> params) {
        this.params = params;
        return this;
    }

    // ----------------------------data--------------------------
    /**
     * 发送到服务器的查询字符串：name=value&name2=value2
     * 最终是以HttpURLConnection.getOutputStream().write(data)的形式发送
     * @param data
     * @return
     */
    public Http data(String data) {
        this.data = data;
        return this;
    }

    /**
     * 发送到服务器的数据
     * @param params
     * @param charset
     * @return
     */
    public Http data(Map<String, ?> params, String charset) {
        this.data = HttpParams.buildParams(params, charset);
        return this;
    }

    /**
     * 发送到服务器的数据
     * @param params
     * @return
     */
    public Http data(Map<String, ?> params) {
        return data(params, HttpRequest.CHARSET_UTF8);
    }

    // ----------------------------part--------------------------
    /**
     * 文件上传
     * @param name
     * @param filename
     * @param mime
     * @return
     */
    public Http part(String name, String filename, Object mime) {
        this.parts.add(new MimePart(name, filename, mime));
        return this;
    }

    // ----------------------------encode--------------------------
    /**
     * 编码url
     * @param encode
     * @return
     */
    public Http encode(Boolean encode) {
        this.encode = encode;
        return this;
    }

    // ----------------------------request contentType--------------------------
    /**
     * <pre>
     *  发送信息至服务器时内容编码类型，默认：application/x-www-form-urlencoded
     *  调用方式：contentType("application/json", "utf-8")
     * </pre>
     * @param contentType
     * @param contentCharset
     * @return
     */
    public Http contentType(String contentType, String contentCharset) {
        this.contentType = contentType;
        this.contentCharset = contentCharset;
        return this;
    }

    public Http contentType(String contentType) {
        return this.contentType(contentType, HttpRequest.CHARSET_UTF8);
    }

    // ----------------------------response accept--------------------------
    /**
     * 内容类型发送请求头，告诉服务器什么样的响应会接受返回
     * header("Accept", accept)
     * @param accept  application/json
     * @return
     */
    public Http accept(String accept) {
        this.accept = accept;
        return this;
    }

    // ----------------------------timeout--------------------------
    /**
     * set connect timeout
     * @param seconds (s)
     * @return this
     */
    public Http connTimeoutSeconds(int seconds) {
        this.connectTimeout = seconds * 1000;
        return this;
    }

    /**
     * set read timeout
     * @param seconds (s)
     * @return this
     */
    public Http readTimeoutSeconds(int seconds) {
        this.readTimeout = seconds * 1000;
        return this;
    }

    // ----------------------------trust spec cert--------------------------
    /**
     * trust spec certificate
     * @param factory
     * @return
     */
    public Http setSSLSocketFactory(SSLSocketFactory factory) {
        this.sslSocketFactory = factory;
        return this;
    }

    public <T> T request(JavaType type) {
        return Jsons.NORMAL.parse(request(), type);
    }

    public <T> T request(Class<T> type) {
        return Jsons.NORMAL.parse(request(), type);
    }

    /**
     * 发送请求获取响应数据
     * @return
     */
    public String request() {
        HttpRequest request = _request();
        try {
            return request.body();
        } finally {
            disconnect(request);
        }
    }

    /**
     * 下载文件
     * @param output
     */
    public void receive(OutputStream output) {
        HttpRequest request = _request();
        BufferedOutputStream bos = null;
        try {
            if (request.ok()) {
                bos = new BufferedOutputStream(output);
                request.receive(bos);
            } else {
                throw new HttpException("request fail, status: " + request.code());
            }
        } finally {
            disconnect(request);
            if (bos != null) try {
                bos.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public void receive(String filepath) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(filepath);
            receive(out);
        } catch (FileNotFoundException e) {
            throw new HttpException("file not found: " + filepath, e);
        } finally {
            if (out != null) try {
                out.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * 下载文件
     * @return
     */
    public byte[] receive() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        receive(output);
        return output.toByteArray();
    }

    private HttpRequest _request() {
        HttpRequest request;
        switch (method) {
            case GET:
                request = HttpRequest.get(url, params, encode);
                break;
            case POST:
                request = HttpRequest.post(url, params, encode);
                break;
            default:
                throw new UnsupportedOperationException("unsupported http method " + method.name());
        }

        request.trustAllCerts().trustAllHosts().connectTimeout(connectTimeout);
        request.readTimeout(readTimeout).headers(headers).acceptGzipEncoding().uncompress(true);

        if (!StringUtils.isEmpty(contentType)) {
            request.contentType(contentType, contentCharset);
        }

        if (!StringUtils.isEmpty(accept)) {
            request.accept(accept);
        }

        if (this.sslSocketFactory != null) {
            request.setSSLSocketFactory(this.sslSocketFactory);
        }

        if (!StringUtils.isEmpty(data)) {
            request.send(data);
        }

        if (parts != null && !parts.isEmpty()) {
            byte[] data;
            InputStream stream;
            String contentType = null;
            try {
                for (MimePart part : parts) {
                    if (byte[].class.isInstance(part.mime)) {
                        data = (byte[]) part.mime;
                        stream = new ByteArrayInputStream(data);
                    } else {
                        stream = (InputStream) part.mime;
                        data = IOUtils.toByteArray(stream);
                    }
                    try {
                        contentType = Magic.getMagicMatch(data).getMimeType();
                    } catch (MagicMatchNotFoundException e) {
                        //contentType = "text/plain";
                    }
                    request.part(part.name, part.filename, contentType, stream);
                }
            } catch (Exception e) {
                throw new HttpException(e);
            }
        }

        return request;
    }

    private void disconnect(HttpRequest request) {
        if (request != null) try {
            request.disconnect();
        } catch (Exception e) {
            // ignored
        }
    }

    /**
     * http method
     */
    private static enum HttpMethod {
        GET, POST, PUT, DELETE
    }

    /**
     * 文件
     */
    private static final class MimePart {
        private String name;
        private String filename;
        private Object mime;

        MimePart(String name, String filename, Object mime) {
            if (mime instanceof InputStream || mime instanceof byte[]) {
                this.mime = mime;
            } else if (mime instanceof Byte[]) {
                this.mime = ArrayUtils.toPrimitive((Byte[]) mime);
            } else {
                throw new IllegalArgumentException("mime must be InputStream or byte[].");
            }
            this.name = name;
            this.filename = filename;
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        System.out.println(Bytes.hexDump(Http.get("http://www.apachelounge.com/download/VC14/binaries/httpd-2.4.25-win64-VC14.zip").receive()));
        System.out.println("\r\n");
        //System.out.println(Bytes.hexDump(Http.get("http://www.stockstar.com").receive()));
        System.out.println("\r\n");
        Http.get("http://www.baidu.com").receive("d:/baidu.html");
    }
}
