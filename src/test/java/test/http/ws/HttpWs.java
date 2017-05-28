package test.http.ws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;


//    import org.apache.cxf.endpoint.Client;
//    import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;

/**
 * 功能描述：WebService调用
 * 
 */
public class HttpWs
{

    /**
     * 功能描述：HTTP-GET
     * 
     */
    public String get()
    {
        URL url;
        BufferedReader bin = null;
        StringBuilder result = new StringBuilder();
        try
        {
            String urlTemp = "http://www.webxml.com.cn//WebServices/WeatherWebService.asmx/getSupportCity?byProvinceName="
                             + URLEncoder.encode("福建", "UTF-8");// URLEncoder用来参数编码
            url = new URL(urlTemp);
            InputStream in = url.openStream(); // 请求
            bin = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String s = null;
            while ( (s = bin.readLine()) != null)
            {
                result.append(s);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (null != bin)
            {
                try
                {
                    bin.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return result.toString();
    }

    /**
     * 功能描述：HTTP-POST
     * 
     */
    public String post()
    {
        OutputStreamWriter out = null;
        StringBuilder sTotalString = new StringBuilder();
        try
        {
//            URL urlTemp = new URL("https://wwsso.szjs.gov.cn:8084/wwssoManager/services/externalCall/getVersion");
            URL urlTemp = new URL("http://10.210.201.129:8080/idm/services/IdmWebService/getUserInfoDetail");
            URLConnection connection = urlTemp.openConnection();
            connection.setDoOutput(true);
            
//            connection.setDoInput(true);
//            out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
//            StringBuffer sb = new StringBuffer();
//            sb.append("userId=45263&operatorId=93ce5274575740e994a0c458488ce9db");
//            out.write(sb.toString());
//            out.flush();
            
            String sCurrentLine;
            sCurrentLine = "";
            InputStream l_urlStream;
            l_urlStream = connection.getInputStream();// 请求
            BufferedReader l_reader = new BufferedReader(new InputStreamReader(l_urlStream));
            while ( (sCurrentLine = l_reader.readLine()) != null)
            {
                sTotalString.append(sCurrentLine);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (null != out)
            {
                try
                {
                    out.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return sTotalString.toString();
    }

    /**
     * 功能描述： 请求 HTTP-SOAP
     * 
     */
    public String getSoapInputStream()
    {
        try
        {
            String soap = "<?xml version=\"1.0\" encoding=\"utf-8\"?><soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><getSupportCity xmlns=\"http://WebXml.com.cn/\"><byProvinceName></byProvinceName></getSupportCity></soap:Body></soap:Envelope>";
            URL url = new URL("http://www.webxml.com.cn/WebServices/WeatherWebService.asmx?wsdl");
            URLConnection conn = url.openConnection();
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            conn.setRequestProperty("Content-Length", Integer.toString(soap.length()));
            conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            conn.setRequestProperty("SOAPAction", "http://WebXml.com.cn/getSupportCity");

            OutputStream os = conn.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, "utf-8");
            osw.write(soap);
            osw.flush();
            osw.close();
            StringBuilder sTotalString = new StringBuilder();
            String sCurrentLine = "";
            InputStream is = conn.getInputStream();
            BufferedReader l_reader = new BufferedReader(new InputStreamReader(is));
            while ( (sCurrentLine = l_reader.readLine()) != null)
            {
                sTotalString.append(sCurrentLine);
            }
            return sTotalString.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 功能描述：使用CXF 请求 HTTP-SOAP
     * 
     */
    //        public String soap() {
    //            JaxWsDynamicClientFactory clientFactory = JaxWsDynamicClientFactory
    //                    .newInstance();
    //            String url = "http://www.webxml.com.cn/webservices/qqOnlineWebService.asmx?wsdl";// http://www.fjyxd.com:17001/DefDispatcher/dispatche?wsdl
    //            Client clientTemp = clientFactory.createClient(url);
    //            Object[] arg;
    //            String result = "";
    //            try {
    //                arg = clientTemp.invoke("qqCheckOnline", "8698053");// 查询8698053在线状态，QQ号码
    //                // String，默认QQ号码：8698053。返回数据：String，Y
    //                // = 在线；N = 离线；E
    //                // = QQ号码错误；A =
    //                // 商业用户验证失败；V =
    //                // 免费用户超过数量
    //                result = (String) arg[0];
    //            } catch (Exception e) {
    //                e.printStackTrace();
    //            }
    //            return result;
    //        }

    /**
     * 功能描述：调用
     * 
     */
    public static void main(String[] args)
    {
        HttpWs ct = new HttpWs();
        //        System.out.println("HTTP-GET结果：" + ct.get());
        System.out.println("HTTP-POST结果：" + ct.post());
        //        System.out.println("HTTP-SOAP结果：" + ct.getSoapInputStream());
        //            System.out.println("CXF HTTP-SOAP结果：" + ct.soap());
    }

}
