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
        KeyStoreResolver r = new KeyStoreResolver(KeyStoreType.PKCS12, ResourceLoaderFacade.getResource("cas_test.pfx").getStream(), "1234");

        byte[] img = Streams.input2bytes(ResourceLoaderFacade.getResource("2.png").getStream());
        Signer signer = new Signer(r.getPrivateKey("1234"), r.getX509CertChain(), img);
        
        Stamp stamp1 = new Stamp(1,100,250);
        Stamp stamp2 = new Stamp(1,300,250);
        
        byte[] pdf = Streams.input2bytes(ResourceLoaderFacade.getResource("ElasticSearch.pdf").getStream());
        
        byte[] result = PdfSignature.sign(pdf, new Stamp[]{stamp1, stamp2}, signer);
        Streams.bytes2file(result, "d:/test/123.pdf");
        
    }
}
