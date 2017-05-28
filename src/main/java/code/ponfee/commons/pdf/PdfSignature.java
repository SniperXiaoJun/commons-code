package code.ponfee.commons.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Calendar;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfSignatureAppearance.RenderingMode;

import code.ponfee.commons.util.ImageUtils;

import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * pdf签章
 * @author fupf
 */
public class PdfSignature {

    public static final float SIGN_AREA_RATE = 0.8f;

    /**
     * 对pdf签名
     * @param pdf
     * @param stamp
     * @param signer
     * @return
     */
    public static byte[] sign(byte[] pdf, Stamp stamp, Signer signer) {
        PdfReader reader = null;
        PdfStamper stp = null;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(1024 * 1024);
            reader = new PdfReader(pdf);

            //stp = PdfStamper.createSignature(reader, out, '\0');
            stp = PdfStamper.createSignature(reader, out, '\0', null, true); // 最后一个参数为true，允许对同一文档多次签名

            PdfSignatureAppearance sap = stp.getSignatureAppearance();
            sap.setCrypto(signer.getPriKey(), signer.getCertChain(), null, PdfSignatureAppearance.VERISIGN_SIGNED);
            //sap.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
            sap.setCertificationLevel(PdfSignatureAppearance.NOT_CERTIFIED);
            /*sap.setReason("");
            sap.setLocation("");
            sap.setContact("");
            sap.setProvider("");*/

            Image image = signer.getImage();
            sap.setSignDate(Calendar.getInstance()); // 设置签名时间为当前日期
            sap.setSignatureGraphic(image);
            sap.setAcro6Layers(true);
            sap.setRenderingMode(RenderingMode.GRAPHIC);
            String flag = "pdf seal[" + System.nanoTime() + "]";
            /*Rectangle mediabox = reader.getPageSize(stamp.getPageNum());
            float bottom = stamp.getTop() - image.getHeight();
            float top = mediabox.getTop() - stamp.getTop();
            float bottom = top - image.getHeight();*/

            float urx = stamp.getLeft() + image.getWidth() * SIGN_AREA_RATE;
            float ury = stamp.getBottom() + image.getTop() * SIGN_AREA_RATE;
            Rectangle rectangle = new Rectangle(stamp.getLeft(), stamp.getBottom(), urx, ury);
            sap.setVisibleSignature(rectangle, stamp.getPageNum(), flag);
            PdfWriter writer = stp.getWriter();
            stp.close();
            stp = null;
            writer.setCompressionLevel(5);
            writer.flush();
            out.flush();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (reader != null) try {
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (stp != null) try {
                stp.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 签多次
     * @param pdf
     * @param stamps
     * @param signer
     * @return
     */
    public static byte[] sign(byte[] pdf, Stamp[] stamps, Signer signer) {
        byte[] bytes = sign(pdf, stamps[0], signer);
        for (int i = 1; i < stamps.length; i++) {
            if (stamps[i] == null) continue;
            bytes = sign(bytes, stamps[i], signer);
        }
        return bytes;
    }

    /**
     * 印章相关信息
     */
    public static class Stamp {

        private float left;
        private float bottom;
        private int pageNum;

        public float getLeft() {
            return left;
        }

        public void setLeft(float left) {
            this.left = left;
        }

        public int getPageNum() {
            return pageNum;
        }

        public void setPageNum(int pageNum) {
            this.pageNum = pageNum;
        }

        public float getBottom() {
            return bottom;
        }

        public void setBottom(float bottom) {
            this.bottom = bottom;
        }
    }

    /**
     * 签名者
     */
    public static class Signer {

        private final PrivateKey priKey;
        private final Certificate[] certChain;
        private final Image image;
        private boolean transparent = true;

        public Signer(PrivateKey priKey, Certificate[] certChain, byte[] img) {
            this.priKey = priKey;
            this.certChain = certChain;
            if (transparent) {
                img = ImageUtils.transparent(img, 250, 235);
            }
            try {
                this.image = Image.getInstance(img);
            } catch (BadElementException | IOException e) {
                throw new IllegalArgumentException(e);
            }
        }

        public PrivateKey getPriKey() {
            return priKey;
        }

        public Certificate[] getCertChain() {
            return certChain;
        }

        public Image getImage() {
            return image;
        }
    }

}
