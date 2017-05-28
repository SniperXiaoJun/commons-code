package test.http.ws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;

public class PostWS {

    public static void main(String[] args) throws Exception {
        System.out.println(new BigDecimal("1"));
        
        
//        testJaxws();
//        testSoap();
//        testPost();
    }

    private static void testSoap() throws Exception {
        SOAPMessage message = MessageFactory.newInstance().createMessage();
        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
       
//        SOAPHeader header = envelope.getHeader();
//        if (header == null) header = envelope.addHeader();
//        header.addHeaderElement( new QName("http://www.tyky.com.cn/cMashup/" , "license" , "tns")).setValue("this a license" );
       
        SOAPBody body = envelope.getBody();
        SOAPBodyElement elem = body.addBodyElement(new QName("http://www.tyky.com.cn/cMashup/", "UpdateCAStatusResult", "tns"));
        
        SOAPElement arrayOfKeyValueElem = elem.addChildElement("ArrayOfKeyValueOfstringstring");
        
        SOAPElement keyValueElem1 = arrayOfKeyValueElem.addChildElement("KeyValueOfstringstring");
        keyValueElem1.addChildElement( "Key").setValue("123456" );
        keyValueElem1.addChildElement( "value").setValue("org" );
       
        SOAPElement keyValueElem2 = arrayOfKeyValueElem.addChildElement("KeyValueOfstringstring");
        keyValueElem2.addChildElement( "Key").setValue("0001" );
        keyValueElem2.addChildElement( "value").setValue("user" );
        
        
        URL url = new URL("http://112.95.149.106:8088/SystemPadServices.svc?wsdl" );
        QName qName = new QName("http://tempuri.org/", "SystemPadService");
        Service service = Service.create(url, qName);
        Dispatch<SOAPMessage> dispatch = service.createDispatch(new QName("http://www.tyky.com.cn/cMashup/", "SystemPadServicePort"),
                SOAPMessage. class, Service.Mode.MESSAGE);
       
        SOAPMessage msg = dispatch.invoke(message);
       
        //System.out.println(msg.getSOAPBody().getElementsByTagName("addResult").item(0).getTextContent());

    }
    
    public static void testPost() {
        OutputStreamWriter out = null;
        StringBuilder sTotalString = new StringBuilder();
        try {
            URL urlTemp = new URL("http://112.95.149.106:8088/SystemPadServices.svc/UpdateCAStatus/Platform");
            HttpURLConnection connection = (HttpURLConnection)urlTemp.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Content-type", "application/json");
            connection.setRequestMethod("POST");   
            out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
            out.write("{\"caUser\":[{\"Key\":\"123123\",\"Value\":\"org\"}]}");
            out.flush();
            
            String sCurrentLine = "";
            InputStream l_urlStream = connection.getInputStream();// 请求
            BufferedReader l_reader = new BufferedReader(new InputStreamReader(l_urlStream));
            while ((sCurrentLine = l_reader.readLine()) != null) {
                sTotalString.append(sCurrentLine);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != out) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println(sTotalString.toString());
    //        return sTotalString.toString();
    }
}
