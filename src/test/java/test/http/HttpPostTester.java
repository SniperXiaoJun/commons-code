package test.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

@SuppressWarnings("deprecation")
public class HttpPostTester {

    @SuppressWarnings("resource")
    public static String post(String reqURL, Map<String, String> params) throws Exception {
        HttpPost httpPost = new HttpPost(reqURL);
        if (params != null) {
            List<BasicNameValuePair> nvps = new ArrayList<>();
            Set<Entry<String, String>> paramEntrys = params.entrySet();
            for (Entry<String, String> entry : paramEntrys) {
                nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
        }

        httpPost.setHeader("User-Agent", "datagrand/datareport/java sdk v1.0.0");
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

        HttpClient httpClient = new DefaultHttpClient();
        HttpParams httpParams = httpClient.getParams();
        HttpConnectionParams.setSoTimeout(httpParams, 60 * 1000);
        HttpConnectionParams.setConnectionTimeout(httpParams, 60 * 1000);

        HttpResponse response = httpClient.execute(httpPost);
        StatusLine status = response.getStatusLine();
        if (status.getStatusCode() >= HttpStatus.SC_MULTIPLE_CHOICES) {
            System.out.printf("Did not receive successful HTTP response: status code = {}, status message = {}", status.getStatusCode(), status.getReasonPhrase());
            httpPost.abort();
        }

        String responseContent = "";
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            responseContent = EntityUtils.toString(entity, "utf-8");
            EntityUtils.consume(entity);
        } else {
            System.out.printf("Http entity is null! request url is {},response status is {}", reqURL, response.getStatusLine());
        }
        return responseContent;
    }

    public static void main(String[] args) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("appid", "12345");
        params.put("title", "顶顶顶");
        params.put("textid", "435386945382932");
        params.put("text", "3m每天百分之1利息，60元起步，有需要可以联系我，Q49663537，或者关注百度贴吧，老马平台吧！");

        String res;
        try {
            res = post("http://commentapi.datagrand.com/bad_comment/meituan", params);
            System.out.println(res);
        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}
