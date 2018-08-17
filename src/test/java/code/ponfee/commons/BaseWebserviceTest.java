package code.ponfee.commons;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.reflect.GenericUtils;
import code.ponfee.commons.util.ObjectUtils;
import code.ponfee.commons.util.SpringContextHolder;
import code.ponfee.commons.ws.JAXWS;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/application-config.xml" })
public abstract class BaseWebserviceTest<T> {

    private static final Set<String> PUBLISHED = new HashSet<>();

    private T client;
    private final String addressUrl;

    protected BaseWebserviceTest() {
        this("http://localhost:8888/test-ws/" + ObjectUtils.uuid32());
    }

    protected BaseWebserviceTest(String url) {
        addressUrl = url;
    }

    protected final T client() {
        return client;
    }

    @Before
    public final void setUp() {
        Class<T> clazz = GenericUtils.getActualTypeArgument(this.getClass());
        synchronized (BaseWebserviceTest.class) {
            int pos = StringUtils.ordinalIndexOf(addressUrl, "/", 3);
            if (pos == -1) {
                pos = addressUrl.length();
            }
            String prefixUrl = addressUrl.substring(0, pos); // http://domain:port
            if (!PUBLISHED.contains(prefixUrl)) {
                JAXWS.publish(addressUrl, SpringContextHolder.getBean(clazz)); // 发布web service
                PUBLISHED.add(addressUrl);
            } else {
                System.out.println("The web service: " + prefixUrl + " are already published.");
            }
        }

        /*JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(clazz);
        factory.setAddress(addressUrl);
        client = (T) factory.create();*/
        client = JAXWS.client(clazz, addressUrl, "namespaceURI", "localPart");
        initiate();
    }

    @After
    public final void tearDown() {
        destory();
    }

    protected void initiate() {
        // do no thing
    }

    protected void destory() {
        // do no thing
    }

    public static void consloe(Object obj) {
        System.out.println(Jsons.toJson(obj));
    }

}
