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

    public static <T> T find(Class<T> clazz, String address, QName qname) {
        Service service = Service.create(newURL(address), qname);
        return service.getPort(clazz);
    }

    public static <T> T find(Class<T> clazz, String address, String namespaceURI, String localPart) {
        return find(clazz, address, new QName(namespaceURI, localPart));
    }

    public static void publish(String address, Object implementor) {
        Endpoint.publish(address, implementor);
    }

    private static URL newURL(String address) {
        try {
            return new URL(address);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url address: " + address, e);
        }
    }

}
