package code.ponfee.commons.ws;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;

/**
 * jax-ws工具类
 * @author fupf
 */
public class JAXWS {

    public static <T> T client(Class<T> clazz, String address, QName qname) {
        Service service = Service.create(newUrl(address), qname);
        return service.getPort(clazz);
    }

    public static <T> T client(Class<T> clazz, String address, 
                               String namespaceURI, String localPart) {
        return client(clazz, address, new QName(namespaceURI, localPart));
    }

    public static void publish(String address, Object implementor) {
        Endpoint.publish(address, implementor);
    }

    private static URL newUrl(String address) {
        try {
            return new URL(address);
        } catch (MalformedURLException e) {
            // cannot happened
            throw new IllegalArgumentException("Invalid url: " + address, e);
        }
    }

}
