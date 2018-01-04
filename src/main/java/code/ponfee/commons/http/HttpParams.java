package code.ponfee.commons.http;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import code.ponfee.commons.io.Files;
import code.ponfee.commons.util.ObjectUtils;
import code.ponfee.commons.util.UrlCoder;

/**
 * http参数工具类
 * @author Ponfee
 */
public class HttpParams {

    // ----------------------------获取url中的参数----------------------------
    public static Map<String, String[]> parseUrlParams(String url) {
        return parseUrlParams(url, Files.DEFAULT_CHARSET);
    }

    public static Map<String, String[]> parseUrlParams(String url, String charset) {
        int idx = url.indexOf('?');
        return idx == -1 ? null : parseParams(url.substring(idx + 1), charset);
    }

    // ------------------------解析query string中的请求参数-------------------------
    public static Map<String, String[]> parseParams(String queryString) {
        return parseParams(queryString, Files.DEFAULT_CHARSET);
    }

    /**
     * 解析参数
     * @param queryString
     * @param encoding
     * @return
     */
    public static Map<String, String[]> parseParams(String queryString, String encoding) {
        Map<String, String[]> params = new LinkedHashMap<>();
        if (queryString == null || queryString.length() == 0) {
            return params;
        }

        if (encoding == null) {
            encoding = Files.DEFAULT_CHARSET;
        }

        String[] kv;
        for (String param : queryString.split("&")) {
            kv = param.split("=", 2);
            putParam(params, kv[0], kv.length == 1 ? "" : UrlCoder.decodeURIComponent(kv[1], encoding));
        }
        return params;
    }

    // --------------------------------构建参数--------------------------------
    /**
     * 键值对构建参数(默认UTF-8)
     * @param params
     * @return
     */
    public static String buildParams(Map<String, ?> params) {
        return HttpParams.buildParams(params, Files.DEFAULT_CHARSET);
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
        Object value;
        for (Map.Entry<String, ?> entry : params.entrySet()) {
            value = entry.getValue();
            if (value != null && value.getClass().isArray()) {
                values = new String[Array.getLength(value)];
                for (int length = values.length, i = 0; i < length; i++) {
                    values[i] = Objects.toString(Array.get(value, i), "");
                }
            } else {
                values = new String[] { Objects.toString(entry.getValue(), "") };
            }

            for (String val : values) {
                builder.append(entry.getKey())
                       .append("=")
                       .append(UrlCoder.encodeURIComponent(val, encoding))
                       .append("&");
            }
        }

        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }

    // ---------------------------------构建url地址---------------------------------
    /**
     * 构建url地址
     * @param url
     * @param encoding
     * @param params
     * @return
     */
    public static String buildUrlPath(String url, String encoding, Map<String, ?> params) {
        if (params == null || params.isEmpty()) {
            return url;
        }

        return url 
            + (url.indexOf('?') == -1 ? '?' : '&') 
            + buildParams(params, encoding);
    }

    public static String buildUrlPath(String url, String encoding, String... params) {
        if (params.length % 2 != 0) {
            throw new IllegalArgumentException("not pair params");
        }

        Map<String, String> map = new LinkedHashMap<>();
        for (int j = 0; j < params.length; j += 2) {
            map.put(params[j], params[j + 1]);
        }
        return buildUrlPath(url, encoding, map);
    }

    // ---------------------------------构建签名数据---------------------------------
    /**
     * 构建待签名数据
     * @param params 请求参数
     * @return
     */
    public static String buildSigning(Map<String, ?> params) {
        return buildSigning(params, "", null);
    }

    public static String buildSigning(Map<String, ?> params, String[] excludes) {
        return buildSigning(params, "", excludes);
    }

    public static String buildSigning(Map<String, ?> params, String wrapChar, String[] excludes) {
        List<String> filter = (excludes == null || excludes.length == 0)
                              ? Collections.emptyList() 
                              : Arrays.asList(excludes);

        // 过滤参数
        Map<String, String> signingMap = new TreeMap<>();
        for (Map.Entry<String, ?> entry : params.entrySet()) {
            if (!filter.contains(entry.getKey())
                && StringUtils.isNotEmpty(Objects.toString(entry.getValue(), null))) {
                signingMap.put(entry.getKey(), entry.getValue().toString());
            }
        }

        // 拼接待签名串，blank string to prevent if signingMap is empty
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

    // ---------------------------------构建Form表单---------------------------------
    /**
     * 构建form表单
     * @param url
     * @param params
     * @return
     */
    public static String buildForm(String url, Map<String, ?> params) {
        StringBuilder form = new StringBuilder(256);
        String formName = ObjectUtils.uuid22();
        form.append("<form action=\"").append(url).append("\" name=\"")
            .append(formName).append("\" method=\"post\">");

        Object value;
        for (Map.Entry<String, ?> param : params.entrySet()) {
            value = param.getValue();
            if (value != null && value.getClass().isArray()) {
                for (int length = Array.getLength(value), i = 0; i < length; i++) {
                    buildInputElement(form, param.getKey(), Array.get(value, i));
                }
            } else {
                buildInputElement(form, param.getKey(), value);
            }
        }

        return form.append("</form>")
                   .append("<script type=\"text/javascript\">document.forms['")
                   .append(formName)
                   .append("'].submit();</script>")
                   .toString();
    }

    // --------------------------------------private method-----------------------------------
    private static void putParam(Map<String, String[]> params, String name, String value) {
        String[] oldValues = params.get(name);
        if (oldValues == null) {
            params.put(name, new String[] { value });
        } else {
            params.put(name, ArrayUtils.add(oldValues, value));
        }
    }

    private static void buildInputElement(StringBuilder form, String name, Object value) {
        form.append("<input type=\"hidden\" name=\"")
            .append(name)
            .append("\" value=\"")
            .append(Objects.toString(value, ""))
            .append("\" />");
    }

}
