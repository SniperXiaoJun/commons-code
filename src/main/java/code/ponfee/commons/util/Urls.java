package code.ponfee.commons.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public final class Urls {

    private static final String DEFAULT_CHARSET = "UTF-8";

    public static String encodeURI(String url) {
        return encodeURI(url, DEFAULT_CHARSET);
    }

    /**
     * 相当于javascript中的encodeURI
     * 不会被此方法编码的字符：! @ # $& * ( ) = : / ; ? + '
     * @param url
     * @param charset
     * @return
     */
    public static String encodeURI(String url, String charset) {
        StringBuilder builder = new StringBuilder(url.length() * 3 / 2);
        char c;
        byte[] b;
        for (int n = url.length(), i = 0; i < n; i++) {
            c = url.charAt(i);
            if (c >= 0 && c <= 255) {
                builder.append(c);
            } else {
                try {
                    b = Character.toString(c).getBytes(charset);
                } catch (Exception ex) {
                    b = new byte[0];
                }
                for (int j = 0; j < b.length; j++) {
                    int k = b[j];
                    if (k < 0) k += 256;
                    builder.append("%" + Integer.toHexString(k).toUpperCase());
                }
            }
        }
        return builder.toString();
    }

    public static String decodeURI(String url) {
        return decodeURI(url, DEFAULT_CHARSET);
    }

    /**
     * 相当于javascript的decodeURI
     * @param url
     * @param charset
     * @return
     */
    public static String decodeURI(String url, String charset) {
        try {
            return URLDecoder.decode(url, charset);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String encodeURIComponent(String url) {
        return encodeURIComponent(url, DEFAULT_CHARSET);
    }

    /**
     * 相当于javascript中的encodeURIComponent
     * 不会被此方法编码的字符：! * ( )
     * @param url
     * @param charset
     * @return
     */
    public static String encodeURIComponent(String url, String charset) {
        try {
            return URLEncoder.encode(url, charset);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String decodeURIComponent(String url) {
        return decodeURIComponent(url, DEFAULT_CHARSET);
    }

    /**
     * 相当于javascript中的decodeURIComponent
     * @param url
     * @param charset
     * @return
     */
    public static String decodeURIComponent(String url, String charset) {
        try {
            return URLDecoder.decode(url, charset);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        String s = encodeURI("http://www.oschina.net/search?scope=bbs&q=C语言");
        System.out.println(s);
        System.out.println(decodeURI(s));

        s = URLEncoder.encode("http://www.oschina.net/search?scope=bbs&q=C语言", DEFAULT_CHARSET);
        System.out.println(s);
        System.out.println(decodeURIComponent(s));

    }
}
