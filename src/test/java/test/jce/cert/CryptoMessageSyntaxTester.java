package test.jce.cert;

import java.io.FileInputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.junit.Test;

import code.ponfee.commons.http.Http;
import code.ponfee.commons.jce.pkcs.CryptoMessageSyntax;
import code.ponfee.commons.jce.pkcs.PKCS7Signature;
import code.ponfee.commons.jce.security.KeyStoreResolver;
import code.ponfee.commons.jce.security.KeyStoreResolver.KeyStoreType;
import code.ponfee.commons.util.MavenProjects;
import code.ponfee.commons.util.Streams;

public class CryptoMessageSyntaxTester {

    public @Test void testEnvelop() throws Exception {
        KeyStoreResolver resolver = new KeyStoreResolver(KeyStoreType.PKCS12, new FileInputStream("d:/test/subject.pfx"), "123456");
        X509Certificate cert = resolver.getX509CertChain()[0];
        PrivateKey privateKey = resolver.getPrivateKey("123456");
        //byte[] data = Streams.file2bytes(MavenProjects.getTestJavaFile(CMSTester.class));
        byte[] data = Http.get("http://www.baidu.com").receive();
        long start = System.currentTimeMillis();
        //System.out.println(Bytes.hexDump(data));
        System.out.println("origin len------------" + data.length);
        System.out.println("===============================================");

        byte[] enveloped = CryptoMessageSyntax.envelop(data, cert, new ASN1ObjectIdentifier("1.2.840.113549.3.7"));
        System.out.println("cost" + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        //System.out.println(Bytes.hexDump(enveloped));
        System.out.println("enveloped len------------" + enveloped.length);
        System.out.println("===============================================");

        byte[] unveloped = CryptoMessageSyntax.unenvelop(enveloped, privateKey);
        //System.out.println(new String(unveloped));
        System.out.println("===============================================");
    }
    
    public @Test void testSign() throws Exception {
        KeyStoreResolver resolver = new KeyStoreResolver(KeyStoreType.PKCS12, new FileInputStream("d:/test/subject.pfx"), "123456");
        X509Certificate[] certChain = resolver.getX509CertChain();
        PrivateKey privateKey = resolver.getPrivateKey("123456");
        byte[] data = Streams.file2bytes(MavenProjects.getTestJavaFile(CryptoMessageSyntaxTester.class));
        System.out.println("origin len------------" + data.length);
        byte[] signed = CryptoMessageSyntax.sign(data, privateKey, certChain);
        System.out.println("signed len------------" + signed.length);
        CryptoMessageSyntax.verify(signed);
    }
    
    
    public @Test void testSign2() throws Exception {
        KeyStoreResolver resolver = new KeyStoreResolver(KeyStoreType.PKCS12, new FileInputStream("d:/test/cas_test.pfx"), "1234");
        X509Certificate[] certChain = resolver.getX509CertChain();
        PrivateKey privateKey = resolver.getPrivateKey("1234");
        byte[] data = Streams.file2bytes(MavenProjects.getTestJavaFile(CryptoMessageSyntaxTester.class));
        System.out.println("origin len------------" + data.length);
        byte[] signed = PKCS7Signature.sign(privateKey, certChain[0], data, true);
        System.out.println("signed len------------" + signed.length);
        PKCS7Signature.verify(signed);
    }

}
