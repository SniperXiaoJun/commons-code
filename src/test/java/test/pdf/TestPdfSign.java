package test.pdf;

import java.io.IOException;

import code.ponfee.commons.jce.security.KeyStoreResolver;
import code.ponfee.commons.jce.security.KeyStoreResolver.KeyStoreType;
import code.ponfee.commons.pdf.PdfSignature;
import code.ponfee.commons.pdf.PdfSignature.Signer;
import code.ponfee.commons.pdf.PdfSignature.Stamp;
import code.ponfee.commons.resource.ResourceLoaderFacade;
import code.ponfee.commons.util.Streams;

public class TestPdfSign {

    public static void main(String[] args) throws IOException {
        KeyStoreResolver r = new KeyStoreResolver(KeyStoreType.PKCS12, ResourceLoaderFacade.getResource("subject.pfx").getStream(), "123456");

        byte[] img = Streams.input2bytes(ResourceLoaderFacade.getResource("2.png").getStream());
        Signer signer = new Signer(r.getPrivateKey("123456"), r.getX509CertChain(), img);
        
        Stamp stamp1 = new Stamp();
        stamp1.setLeft(100);
        stamp1.setBottom(250);
        stamp1.setPageNum(1);

        Stamp stamp2 = new Stamp();
        stamp2.setLeft(300);
        stamp2.setBottom(250);
        stamp2.setPageNum(1);
        
        byte[] pdf = Streams.input2bytes(ResourceLoaderFacade.getResource("RocketMQ_design.pdf").getStream());
        
        byte[] result = PdfSignature.sign(pdf, new Stamp[]{stamp1, stamp2}, signer);
        Streams.bytes2file(result, "d:/cert/123.pdf");
        
    }
}
