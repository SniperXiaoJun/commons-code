package code.ponfee.commons.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import code.ponfee.commons.util.ObjectUtils;

/**
 * http参数工具类
 * @author fupf
 */
public class HttpParams {

    /**
     * 解析参数
     * @param queryString
     * @param encoding
     * @return
     */
    public static Map<String, String[]> parseParams(String queryString, String encoding) {
        Map<String, String[]> params = new HashMap<>();
        if (queryString == null || queryString.length() <= 0) return params;

        byte[] bytes = null;
        if (encoding == null) {
            bytes = queryString.getBytes();
        } else {
            bytes = queryString.getBytes(Charset.forName(encoding));
        }
        parseParams(params, bytes, encoding);
        return params;
    }

    /**
     * 键值对构建参数(默认UTF-8)
     * @param params
     * @return
     */
    public static String buildParams(Map<String, ?> params) {
        return HttpParams.buildParams(params, "UTF-8");
    }

    /**
     * 键值对构建参数
     * @param params
     * @param encoding
     * @return
     */
    public static String buildParams(Map<String, ?> params, String encoding) {
        StringBuilder builder = new StringBuilder();
        String[] values;
        for (Map.Entry<String, ?> entry : params.entrySet()) {
            if (entry.getValue() instanceof String[]) {
                values = (String[]) entry.getValue();
            } else {
                values = new String[] { Objects.toString(entry.getValue(), "") };
            }

            try {
                for (String value : values) {
                    builder.append(entry.getKey()).append("=")
                           .append(URLEncoder.encode(value, encoding)).append("&");
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }

    /**
     * 构建url地址
     * @param url
     * @param encoding
     * @param params
     * @return
     */
    public static String buildUrlPath(String url, String encoding, Map<String, ?> params) {
        if (params == null || params.isEmpty()) return url;

        String queryString = buildParams(params, encoding);
        return url + (url.indexOf('?') < 0 ? '?' : '&') + queryString;
    }

    public static String buildUrlPath(String url, String encoding, String... params) {
        if (params.length % 2 != 0) {
            throw new IllegalArgumentException("not pair params");
        }

        Map<String, String> map = new HashMap<>();
        for (int j = 0; j < params.length; j += 2) {
            map.put(params[j], params[j + 1]);
        }
        return buildUrlPath(url, encoding, map);
    }

    /**
     * 构建待签名数据
     * @param params 请求参数
     * @return
     */
    public static String buildSigning(Map<String, String> params) {
        return buildSigning(params, "", null);
    }

    public static String buildSigning(Map<String, String> params, String[] excludes) {
        return buildSigning(params, "", excludes);
    }

    public static String buildSigning(Map<String, String> params, String wrapChar, String[] excludes) {
        List<String> filter = ObjectUtils.isEmpty(excludes) 
                              ? Collections.emptyList() 
                              : Arrays.asList(excludes);

        // 过滤参数
        Map<String, String> signingMap = new TreeMap<>();
        for (Map.Entry<String, String> p : params.entrySet()) {
            if (filter.contains(p.getKey()) || StringUtils.isEmpty(p.getValue())) {
                continue;
            }
            signingMap.put(p.getKey(), p.getValue());
        }

        // 拼接待签名串
        StringBuilder signing = new StringBuilder("");
        for (Map.Entry<String, String> entry : signingMap.entrySet()) {
            signing.append(entry.getKey()).append('=').append(wrapChar)
                   .append(entry.getValue()).append(wrapChar).append('&');
        }
        if (signing.length() > 0) {
            signing.deleteCharAt(signing.length() - 1); // 删除未位的'&'
        }
        return signing.toString();
    }

    /**
     * 构建form表单
     * @param url
     * @param params
     * @return
     */
    public static String buildForm(String url, Map<String, String> params) {
        StringBuilder form = new StringBuilder(128);
        String formName = ObjectUtils.uuid22();
        form.append("<form action=\"").append(url).append("\" name=\"")
            .append(formName).append("\" method=\"POST\">");
        for (Map.Entry<String, String> param : params.entrySet()) {
            form.append("<input type=\"hidden\" name=\"").append(param.getKey()).append("\" value=\"")
                .append(StringUtils.defaultString(param.getValue())).append("\" />");
        }

        form.append("<input type=\"submit\" style=\"display:none;\" />")
            .append("</form>")
            .append("<script>document.forms['").append(formName)
            .append("'].submit();</script>");

        return form.toString();
    }

    // --------------------------------------private method-----------------------------------
    private static void parseParams(Map<String, String[]> map, byte[] queryString, String encoding) {
        if (queryString == null || queryString.length == 0) return;

        Charset charset = Charset.forName(encoding);
        int ix = 0, ox = 0;
        String key = null, value = null;
        byte c;
        while (ix < queryString.length) {
            c = queryString[(ix++)];
            switch ((char) c) {
                case '&':
                    value = new String(queryString, 0, ox, charset);
                    if (key != null) {
                        putMapEntry(map, key, value);
                        key = null;
                    }
                    ox = 0;
                    break;
                case '=':
                    if (key == null) {
                        key = new String(queryString, 0, ox, charset);
                        ox = 0;
                    } else {
                        queryString[(ox++)] = c;
                    }
                    break;
                case '+':
                    queryString[(ox++)] = 32;
                    break;
                case '%':
                    queryString[(ox++)] = (byte) ((decodeHex(queryString[(ix++)]) << 4) + decodeHex(queryString[(ix++)]));
                    break;
                default:
                    queryString[(ox++)] = c;
                    break;
            }
        }

        if (key != null) {
            value = new String(queryString, 0, ox, charset);
            putMapEntry(map, key, value);
        }
    }

    private static void putMapEntry(Map<String, String[]> map, String name, String value) {
        String[] newValues = null;
        String[] oldValues = (String[]) map.get(name);
        if (oldValues == null) {
            newValues = new String[1];
            newValues[0] = value;
        } else {
            newValues = new String[oldValues.length + 1];
            System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
            newValues[oldValues.length] = value;
        }
        map.put(name, newValues);
    }

    private static byte decodeHex(byte b) {
        if ((b >= 48) && (b <= 57)) return (byte) (b - 48);
        if ((b >= 97) && (b <= 102)) return (byte) (b - 97 + 10);
        if ((b >= 65) && (b <= 70)) return (byte) (b - 65 + 10);
        return 0;
    }

    public static void main(String[] args) {
        //String str = "service=http%3A%2F%2Flocalhost%2Fcas-client%2F&fdsa=fds人a";
        //Map<String, String[]> map = parseParams(str, "UTF-8");
        //System.out.println(((String[]) map.get("fdsa"))[0]);
        //System.out.println(buildParams(map, "utf-8"));

        Map<String, String> map = new HashMap<String, String>();
        map.put("a", "1");
        map.put("b", "2");
        map.put("merReserved", "{a=1&b=2}");
        String s = HttpParams.buildParams(map, "utf-8");
        System.out.println(HttpParams.parseParams(s, "utf-8").get("merReserved")[0]);

        System.out.println(buildUrlPath("/index.html", "utf-8", map));
        
        System.out.println(buildForm("http://localhost:8080", map));
    }
}
