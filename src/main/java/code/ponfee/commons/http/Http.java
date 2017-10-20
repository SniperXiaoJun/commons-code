package code.ponfee.commons.http;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JavaType;

import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.util.Bytes;

/**
 * <pre>
 *  Accept属于请求头， Content-Type属于实体头
 *     请求方的http报头结构：通用报头|请求报头|实体报头 
 *     响应方的http报头结构：通用报头|响应报头|实体报头
 *
 *  <p>
 *  请求报头有：Accept、Accept-Charset、Accept-Encoding、Accept-Language、Referer、
 *          Authorization、From、Host、If-Match、User-Agent、If-Modified-Since等
 *  Accept：告诉WEB服务器自己接受什么介质类型，*∕*表示任何类型，type∕*表示该类型下的所有子类型，type∕sub-type，如Accept(text/html)
 *
 *  <p>
 *  响应报头有：Age、Server、Accept-Ranges、Vary等
 *
 *  <p>
 *  实体报头有：Allow、Location、Content-Base、Content-Encoding、Content-Length、
 *          Content-Range、Content-MD5、Content-Type、Expires、Last-Modified等
 *  Content-Type：WEB服务器告诉浏览器自己响应的消息格式，例如Content-Type(application/xml)
 * </pre>
 * http://www.atool.org/httptest.php
 * 
 * http工具类
 * @author fupf
 */
public final class Http {

    private final String url; // url
    private final HttpMethod method; // 请求方法

    private final Map<String, String> headers = new HashMap<>();   // http请求头
    private final Map<String, Object> params  = new HashMap<>();   // http请求参数
    private final List<MimePart> parts        = new ArrayList<>(); // http文件上传

    private String data; // 请求data
    private int connectTimeout = 1000 * 5; // 连接超时时间
    private int readTimeout = 1000 * 5; // 读取返回数据超时时间
    private Boolean encode = Boolean.TRUE; // 是否编码
    private String contentType; // 请求内容类型
    private String contentCharset; // 请求内容编码
    private String accept; // 接收类型
    private SSLSocketFactory sslSocketFactory; // 走SSL/TSL通道

    private Http(String url, HttpMethod method) {
        this.url = url;
        this.method = method;
    }

    // ----------------------------method--------------------------
    public static Http get(String url) {
        return new Http(url, HttpMethod.GET);
    }

    public static Http post(String url) {
        return new Http(url, HttpMethod.POST);
    }

    public static Http put(String url) {
        return new Http(url, HttpMethod.PUT);
    }

    public static Http head(String url) {
        return new Http(url, HttpMethod.HEAD);
    }

    public static Http delete(String url) {
        return new Http(url, HttpMethod.DELETE);
    }

    public static Http trace(String url) {
        return new Http(url, HttpMethod.TRACE);
    }

    public static Http options(String url) {
        return new Http(url, HttpMethod.OPTIONS);
    }

    // ----------------------------header--------------------------
    /**
     * 设置请求头
     * @param name
     * @param value
     * @return
     */
    public Http addHeader(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    /**
     * 设置请求头
     * @param headers
     * @return
     */
    public Http addHeader(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }

    // ----------------------------params--------------------------
    /**
     * 最终是拼接成queryString的形式追加到url（即作为get的http请求参数）
     * get方式会有编码等问题，推荐使用data方式传参数：{@link #data(Map)}
     * @param params
     * @return
     */
    public Http addParam(Map<String, ? extends Object> params) {
        this.params.putAll(params);
        return this;
    }

    public <T extends Object> Http addParam(String name, T value) {
        this.params.put(name, value);
        return this;
    }

    // ----------------------------data--------------------------
    /**
     * 发送到服务器的数据
     * @param params
     * @return
     */
    public Http data(Map<String, ?> params) {
        return data(params, HttpRequest.CHARSET_UTF8);
    }

    /**
     * 发送到服务器的数据
     * @param params
     * @param charset
     * @return
     */
    public Http data(Map<String, ?> params, String charset) {
        return data(HttpParams.buildParams(params, charset));
    }

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

    // ----------------------------part--------------------------
    /**
     * 文件上传
     * @param name
     * @param filename
     * @param mime
     * @return
     */
    public Http addPart(String name, String filename, Object mime) {
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

    // --------------------------------timeout------------------------------
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

    // --------------------------------request------------------------------
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
        HttpRequest request = request0();
        try {
            return request.body();
        } finally {
            disconnect(request);
        }
    }

    public void download(String filepath) {
        try (OutputStream out = new FileOutputStream(filepath)) {
            download(out);
        } catch (IOException e) {
            throw new HttpException("download error: " + filepath, e);
        }
    }

    public byte[] download() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        download(output);
        return output.toByteArray();
    }

    /**
     * http下载
     * @param output    output to stream of response data
     */
    public void download(OutputStream output) {
        HttpRequest request = request0();
        BufferedOutputStream bos = null;
        try {
            if (request.ok()) {
                bos = new BufferedOutputStream(output);
                request.receive(bos);
            } else {
                throw new HttpException("request failed, status: " + request.code());
            }
        } finally {
            disconnect(request);
            if (bos != null) try {
                bos.close();
            } catch (IOException ignore) {
                ignore.printStackTrace();
            }
        }
    }

    // ----------------------------private methods--------------------------
    private HttpRequest request0() {
        HttpRequest request;
        switch (method) {
            case GET:
                request = HttpRequest.get(url, params, encode);
                break;
            case POST:
                request = HttpRequest.post(url, params, encode);
                break;
            case PUT:
                request = HttpRequest.put(url, params, encode);
                break;
            case HEAD:
                request = HttpRequest.head(url, params, encode);
                break;
            case DELETE:
                request = HttpRequest.delete(url, params, encode);
                break;
            case TRACE:
                request = HttpRequest.trace(url);
                break;
            case OPTIONS:
                request = HttpRequest.options(url);
                break;
            default:
                throw new UnsupportedOperationException("unsupported http method " + method.name());
        }

        if (!StringUtils.isEmpty(contentType)) {
            request.contentType(contentType, contentCharset);
        }

        if (!StringUtils.isEmpty(accept)) {
            request.accept(accept);
        }

        if (!StringUtils.isEmpty(data)) {
            request.send(data);
        }

        for (MimePart part : parts) {
            request.part(part.name, part.fileName, null, part.stream);
        }

        if (this.sslSocketFactory != null) {
            request.setSSLSocketFactory(this.sslSocketFactory);
        } else {
            request.trustAllCerts();
        }

        request.connectTimeout(connectTimeout).readTimeout(readTimeout)
               .trustAllHosts().headers(headers).acceptGzipEncoding().decompress(true);
        return request;
    }

    private static void disconnect(HttpRequest request) {
        if (request != null) try {
            request.disconnect();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    /**
     * http method
     */
    private static enum HttpMethod {
        GET, POST, PUT, DELETE, HEAD, TRACE, OPTIONS;
    }

    /**
     * 文件
     */
    private static final class MimePart {
        private final String name;        // 表单域字段名
        private final String fileName;    // 文件名
        private final InputStream stream; // 文件流

        MimePart(String name, String fileName, Object mime) {
            if (mime instanceof byte[]) {
                this.stream = new ByteArrayInputStream((byte[]) mime);
            } else if (mime instanceof Byte[]) {
                this.stream = new ByteArrayInputStream(ArrayUtils.toPrimitive((Byte[]) mime));
            } else if (mime instanceof String || mime instanceof File) {
                File file = (mime instanceof File) ? (File) mime : new File((String) mime);
                try {
                    this.stream = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    throw new IllegalArgumentException(e);
                }
            } else if (mime instanceof InputStream) {
                this.stream = (InputStream) mime;
            } else {
                throw new IllegalArgumentException("mime must be file path or file or byte array or input stream.");
            }
            this.name = name;
            this.fileName = fileName;
        }
    }

    public static void main(String[] args) throws Exception {
        //System.out.println(Bytes.hexDump(Http.get("http://www.apachelounge.com/download/VC14/binaries/httpd-2.4.25-win64-VC14.zip").download()));
        //Http.get("https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-5.5.1.zip").download(new FileOutputStream("d:/elasticsearch-5.5.1.zip"));
        System.out.println(Bytes.hexDump(Http.get("http://www.stockstar.com").download()));
        //Http.get("http://www.baidu.com").download("d:/baidu.html");
        //System.out.println(Http.get("http://localhost:8081/audit/getImg").data(ImmutableMap.of("imgPath", "imgPath")).request());
        //String[] params = new String[]{"{\"analyze_type\":\"mine_all_cust\",\"date_type\":4,\"class_name\":\"\"}", "{\"analyze_type\":\"mine_all_cust\",\"date_type\":4,\"class_name\":\"衬衫\"}"};
        //Http.post("http://10.118.58.156:8080/market/custgroup/kanban/count/recommend").data(ImmutableMap.of("conditions[]", params)).request();
        @SuppressWarnings("unchecked") 
        Map<String, Object> resp = Http.post("http://10.118.58.74:8080/uploaded/file")
                                       .addParam("param1", "test1213")
                                       .addPart("uploadFile", "abc.pdf", new File("d:/test/abc.pdf"))
                                       .addPart("uploadFile", "word.pdf", new File("d:/test/word.pdf"))
                                       .contentType("multipart/form-data", "UTF-8") // <input type="file" name="upload" />
                                       //.contentType("application/json", "UTF-8") // @RequestBody
                                       //.contentType("application/x-www-form-urlencoded", "UTF-8") // form data
                                       .accept("application/json") // @ResponseBody
                                       //.setSSLSocketFactory(factory) // client cert
                                       .request(Map.class);
        System.out.println(resp);
    }
}
