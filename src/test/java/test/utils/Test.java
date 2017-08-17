package test.utils;

import org.apache.commons.io.IOUtils;

import code.ponfee.commons.resource.ResourceLoaderFacade;
import net.sf.jmimemagic.Magic;

public class Test {

    public static void main(String[] args) throws Exception {
        byte[] img = IOUtils.toByteArray(ResourceLoaderFacade.getResource("2.png").getStream());
        String type = Magic.getMagicMatch(img).getMimeType();
        System.out.println(type);
    }
}
