package test.utils;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import code.ponfee.commons.resource.ResourceLoaderFacade;
import code.ponfee.commons.util.ObjectUtils;
import net.sf.jmimemagic.Magic;

public class Test {

    public static void main(String[] args) throws Exception {
        byte[] img = IOUtils.toByteArray(ResourceLoaderFacade.getResource("2.png").getStream());
        String type = Magic.getMagicMatch(img).getMimeType();
        System.out.println(type);
        
        Set<String> set = new HashSet<>(204800000);
        for (int i = 0; i < 50; i++) {
            new Thread(){
                @Override
                public void run() {
                    String uuid;
                    while(true) {
                        uuid = ObjectUtils.uuid(16);
                        if (!set.add(uuid)) {
                            System.err.println(uuid);
                        }
                    }
                }
                
            }.start();
        }
    }
}
