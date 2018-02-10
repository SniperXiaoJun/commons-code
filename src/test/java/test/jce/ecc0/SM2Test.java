package test.jce.ecc0;

import java.util.Base64;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import code.ponfee.commons.jce.ECParameters;
import code.ponfee.commons.jce.sm.SM2;
import code.ponfee.commons.util.MavenProjects;

public class SM2Test {
    static ECParameters ecParameter = ECParameters.SM2_BEST;

    @Test
    public void test1() {
        byte[] data = MavenProjects.getMainJavaFileAsLineString(SM2.class).substring(0, 100).getBytes();
        Map<String, byte[]> keyMap = SM2.generateKeyPair(ecParameter);
        
        System.out.println(Base64.getUrlEncoder().encodeToString(SM2.getPublicKey(keyMap)));
        System.out.println(Base64.getUrlEncoder().encodeToString(SM2.getPrivateKey(keyMap)));
        
        
        byte[] encrypted = SM2.encrypt(ecParameter, SM2.getPublicKey(keyMap), data);
        System.out.println(Base64.getUrlEncoder().encodeToString(encrypted));
        
        byte[] decrypted = SM2.decrypt(ecParameter, SM2.getPrivateKey(keyMap), encrypted);
        System.out.println(new String(decrypted));
    }
    
    @Test
    public void test2() throws Exception {
        byte[] privateKey = Hex.decodeHex("19F4279987CACA6780592C2B330E932B7C6C27919ED56D63785E398A7C3B568F");
        byte[] encrypted = Hex.decodeHex("04AD22DF19CC60E231B74E4F510537C9D71EB57D3734F7EC44188E5655374BC742469A3BF8D247769AF712A06A4D2EAC24470F9851AB6A2F106600E3B5D8DADD2DCD81D6C781A26C7DCBACD9F947A461FF555537A75E9A19B92EE6E447373C9B776CF0236BCA769A7515795A32AF00F94E9B5EBCDE9B8774F19129815AF04C7D03122982448AFB586F8895C2A695FFC034B1B77E350E925E0BC04A683759C763AC396FC3EFDD9EF4A09E53EFFCCA80C29B42B11AB82A7CEACBBF2E748E028035E4D4E71693");
        byte[] decrypted = SM2.decrypt(ecParameter, privateKey, encrypted);
        System.out.println(new String(decrypted));
    }
}
