package test.pdf;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.HashMap;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfDate;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfPKCS7;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignature;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.TSAClient;

import code.ponfee.commons.jce.pkcs.PKCS1Signature;
import code.ponfee.commons.jce.security.KeyStoreResolver;
import code.ponfee.commons.jce.security.KeyStoreResolver.KeyStoreType;
import code.ponfee.commons.resource.ResourceLoaderFacade;
import code.ponfee.commons.util.Bytes;
import code.ponfee.commons.util.Streams;


public class PdfP7Sign
{
    private static void sign(String src, String dest)
        throws Exception
    {
        // ------------------------------------------------------------------//
        // 1、用户上传自己的证书到服务器，从服务器上拿取待签名文件的hash数据
        KeyStoreResolver resolver = new KeyStoreResolver(KeyStoreType.PKCS12,  ResourceLoaderFacade.getResource("subject.pfx").getStream(), "123456"   );   
        PdfReader reader = new PdfReader(src);
        FileOutputStream fout = new FileOutputStream(dest);
        PdfStamper stp = PdfStamper.createSignature(reader, fout, '\0');
        PdfSignatureAppearance sap = stp.getSignatureAppearance();
        sap.setVisibleSignature(new Rectangle(200, 732, 444, 880), 1, "Signature");
        sap.setCrypto(null, resolver.getX509CertChain(), null, PdfSignatureAppearance.SELF_SIGNED);

        byte[] img = Streams.input2bytes(ResourceLoaderFacade.getResource("2.png").getStream());
        sap.setSignatureGraphic(Image.getInstance(img));
        sap.setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC);

        PdfSignature dic = new PdfSignature(PdfName.ADOBE_PPKLITE, new PdfName(
            "adbe.pkcs7.detached"));
        dic.setReason(sap.getReason());
        dic.setLocation(sap.getLocation());
        dic.setContact(sap.getContact());
        dic.setDate(new PdfDate(sap.getSignDate()));
        sap.setCryptoDictionary(dic);
        int contentEstimated = 15000;
        HashMap<PdfName, Integer> exc = new HashMap<PdfName, Integer>();
        exc.put(PdfName.CONTENTS, new Integer(contentEstimated * 2 + 2));

        sap.preClose(exc);
        InputStream data = sap.getRangeStream();
        MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
        byte buf[] = new byte[8192];
        int n;
        while ( (n = data.read(buf)) > 0)
        {
            messageDigest.update(buf, 0, n);
        }
        byte hash[] = messageDigest.digest();
        // ------------------------------------------------------------------//
        
        
        
        // ------------------------------------------------------------------//
        //2、用户本地签名此HASH数据
        TSAClient tsc = null;
        /*boolean withTS = false;
        if (withTS)
        {
            String tsa_url = properties.getProperty("TSA");
            String tsa_login = properties.getProperty("TSA_LOGIN");
            String tsa_passw = properties.getProperty("TSA_PASSWORD");
            tsc = new TSAClientBouncyCastle(tsa_url, tsa_login, tsa_passw);
        }*/
        byte[] ocsp = null;
        /*boolean withOCSP = false;
        if (withOCSP)
        {
            String url = PdfPKCS7.getOCSPURL((X509Certificate)chain[0]);
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            FileInputStream is = new FileInputStream(properties.getProperty("ROOTCERT"));
            X509Certificate root = (X509Certificate)cf.generateCertificate(is);
            ocsp = new OcspClientBouncyCastle((X509Certificate)chain[0], root, url).getEncoded();
        }*/
        Calendar date = Calendar.getInstance();
        PdfPKCS7 pkcs7 = new PdfPKCS7(resolver.getPrivateKey("123456"), resolver.getX509CertChain(), null, "SHA1", null, false);
        byte[] signed = PKCS1Signature.sign(pkcs7.getAuthenticatedAttributeBytes(hash, date, ocsp), resolver.getPrivateKey("123456"), resolver.getX509CertChain()[0]);
        System.out.println("signed：" + Bytes.base64Encode(signed));
        pkcs7.setExternalDigest(signed, null, "RSA");

        //sgn.update(sh, 0, sh.length);
        byte[] encodedSig = pkcs7.getEncodedPKCS7(hash, date, tsc, ocsp);
        if (contentEstimated + 2 < encodedSig.length)
            throw new DocumentException("Not enough space");
        byte[] paddedSig = new byte[contentEstimated];
        System.arraycopy(encodedSig, 0, paddedSig, 0, encodedSig.length);
        PdfDictionary dic2 = new PdfDictionary();
        dic2.put(PdfName.CONTENTS, new PdfString(paddedSig).setHexWriting(true));
        sap.close(dic2);
    }

    public static void main(String[] args)
        throws Exception
    {
        String src = "D:\\cert\\123.pdf";
        String dest = "D:\\cert\\result.pdf";
        sign(src, dest);
    }
}
