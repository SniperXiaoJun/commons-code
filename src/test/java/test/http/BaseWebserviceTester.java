package test.http;

import java.util.concurrent.atomic.AtomicBoolean;

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
public abstract class BaseWebserviceTester<T> {

    private static final AtomicBoolean IS_PUBLISHED = new AtomicBoolean(false);

    private T client;
    private final Class<T> clazz;
    private final String addressUrl;

    protected BaseWebserviceTester() {
        this("http://localhost:8888/test-ws/" + ObjectUtils.uuid32());
    }

    protected BaseWebserviceTester(String url) {
        clazz = GenericUtils.getActualTypeArgument(this.getClass());
        addressUrl = url;
    }

    protected final T client() {
        return client;
    }

    @Before
    public final void setUp() {
        if (!IS_PUBLISHED.get()) {
            JAXWS.publish(addressUrl, SpringContextHolder.getBean(clazz)); // 发布web service
            IS_PUBLISHED.set(true);
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

    public static void print(Object obj) {
        System.out.println("=======================================" + Jsons.NORMAL.stringify(obj));
    }
}
