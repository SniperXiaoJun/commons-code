package test.http;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import code.ponfee.commons.http.HttpParams;
import code.ponfee.commons.util.ObjectUtils;

public class HttpParamsTest {

    @Test
    public void test1() {
        String str = "service=http%3A%2F%2Flocalhost%2Fcas-client%2F&test=fds中文a";
        System.out.println("\n=============parseParams==============");
        System.out.println(ObjectUtils.toJson(HttpParams.parseParams(str, "UTF-8")));

        System.out.println("\n=============parseUrlParams==============");
        str = "http://localhost:8080/test?service=http%3A%2F%2Flocalhost%2Fcas-client%2F&test=fds中文a";
        System.out.println(ObjectUtils.toJson(HttpParams.parseUrlParams(str)));

        System.out.println("\n=============buildParams==============");
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("a", new String[] { "1" });
        map.put("b", new String[] { "2" });
        map.put("merReserved", new String[] { "{a=1&b=2}" });
        String queryString = HttpParams.buildParams(map, "utf-8");
        System.out.println(queryString);
        System.out.println(ObjectUtils.toJson(HttpParams.parseParams(queryString, "utf-8")));

        System.out.println("\n=============buildUrlPath==============");
        System.out.println(HttpParams.buildUrlPath("/index.html", "utf-8", map));

        System.out.println("\n=============buildForm==============");
        System.out.println(HttpParams.buildForm("http://localhost:8080", map));
    }

    @Test
    public void test2() {
        String url = "http://10.118.58.74:8000/open/api/test?a=1=32=14=12=4=3214=2&abcdef&" + Math.random();
        System.out.println(ObjectUtils.toJson(HttpParams.parseUrlParams(url, "UTF-8")));
    }

    @Test
    public void test3() {
        System.out.println(HttpParams.buildUrlPath("url", "UTF-8", "a", "1","b","2"));
    }
}
