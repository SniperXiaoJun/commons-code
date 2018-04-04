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

    /**
     * Returns a JAX-WS client
     * 
     * @param clazz         the webservice interface, as use {@code WebService} annotation
     * @param address       the wsdl url as http://ip:port/ws/webserviceName?wsdl
     * @param namespaceURI  the targetNamespace of <b>wsdl:definitions</b> attribute
     * @param localPart     the name of <b>wsdl:definitions</b> attribute
     * @return
     */
    public static <T> T client(Class<T> clazz, String address, 
                               String namespaceURI, String localPart) {
        return client(clazz, address, new QName(namespaceURI, localPart));
    }

    public static <T> T client(Class<T> clazz, String address, QName qname) {
        try {
            return Service.create(new URL(address), qname).getPort(clazz);
        } catch (MalformedURLException e) {
            // cannot happened
            throw new IllegalArgumentException("Invalid url: " + address, e);
        }
    }

    /**
     * Server publish the webservice
     * 
     * @param address
     * @param implementor  the webservice interface implements class instance
     */
    public static void publish(String address, Object implementor) {
        Endpoint.publish(address, implementor);
    }

}
