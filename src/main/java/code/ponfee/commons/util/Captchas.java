package code.ponfee.commons.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import com.sun.image.codec.jpeg.ImageFormatException;
import com.sun.image.codec.jpeg.JPEGCodec;

/**
 * 图片验证码生成类
 * @author fupf
 */
@SuppressWarnings("restriction")
public class Captchas {

    //使用到Algerian字体，系统里没有的话需要安装字体，字体只显示大写，去掉了1,0,i,o几个容易混淆的字符
    private static final char[] CODES = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();

    private static final Color[] COLOR_SPACES = { Color.RED, Color.CYAN, Color.GRAY, Color.LIGHT_GRAY,
        Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.WHITE, Color.PINK, Color.BLUE, Color.YELLOW };

    /**
     * 使用系统默认字符源生成验证码
     * @param size 验证码长度
     * @return
     */
    public static String random(int size) {
        return random(size, CODES);
    }

    /**
     * 使用指定源生成验证码
     * @param size 验证码长度
     * @param sources 验证码字符源
     * @return
     */
    public static String random(int size, char[] sources) {
        if (sources == null || sources.length == 0) {
            sources = CODES;
        }
        StringBuilder codes = new StringBuilder(size);
        for (int i = 0, length = sources.length; i < size; i++) {
            codes.append(sources[SecureRandoms.nextInt(length)]);
        }
        return codes.toString();
    }

    /**
     * 输出指定验证码图片流
     * @param width
     * @param height
     * @param out
     * @param code
     */
    public static void generate(int width, int height, OutputStream out, String code) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color[] colors = new Color[5];
        float[] fractions = new float[colors.length];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = COLOR_SPACES[ThreadLocalRandom.current().nextInt(COLOR_SPACES.length)];
            fractions[i] = ThreadLocalRandom.current().nextFloat();
        }
        Arrays.sort(fractions);

        g2.setColor(Color.GRAY);// 设置边框色
        g2.fillRect(0, 0, width, height);

        Color c = getRandColor(200, 250);
        g2.setColor(c);// 设置背景色
        g2.fillRect(0, 2, width, height - 4);

        //绘制干扰线
        g2.setColor(getRandColor(160, 200));// 设置线条的颜色
        for (int i = 0; i < 20; i++) {
            int x = ThreadLocalRandom.current().nextInt(width - 1);
            int y = ThreadLocalRandom.current().nextInt(height - 1);
            int xl = ThreadLocalRandom.current().nextInt(6) + 1;
            int yl = ThreadLocalRandom.current().nextInt(12) + 1;
            g2.drawLine(x, y, x + xl + 40, y + yl + 20);
        }

        // 添加噪点
        float yawpRate = 0.05f;// 噪声率
        int area = (int) (yawpRate * width * height);
        for (int i = 0; i < area; i++) {
            int x = ThreadLocalRandom.current().nextInt(width);
            int y = ThreadLocalRandom.current().nextInt(height);
            int rgb = getRandomIntColor();
            image.setRGB(x, y, rgb);
        }

        shear(g2, width, height, c);// 使图片扭曲

        g2.setColor(getRandColor(100, 160));
        int fontSize = height - 12;
        g2.setFont(new Font("Algerian", Font.ITALIC, fontSize));
        char[] chars = code.toCharArray();
        int size = code.length();
        for (int i = 0; i < size; i++) {
            AffineTransform affine = new AffineTransform();
            affine.setToRotation(Math.PI / 4 * ThreadLocalRandom.current().nextDouble() * 
                                 (ThreadLocalRandom.current().nextBoolean() ? 1 : -1), 
                                 (width / size) * i + fontSize / 2, height / 2);
            g2.setTransform(affine);
            g2.drawChars(chars, i, 1, ((width - 10) / size) * i + 5, height / 2 + fontSize / 2 - 10);
        }

        g2.dispose();

        try {
            JPEGCodec.createJPEGEncoder(out).encode(image); // ImageIO.write(image, "JPEG", os);
        } catch (ImageFormatException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    //-------------------------private methods
    private static Color getRandColor(int fc, int bc) {
        if (fc > 255) {
            fc = 255;
        }
        if (bc > 255) {
            bc = 255;
        }
        int r = fc + ThreadLocalRandom.current().nextInt(bc - fc);
        int g = fc + ThreadLocalRandom.current().nextInt(bc - fc);
        int b = fc + ThreadLocalRandom.current().nextInt(bc - fc);
        return new Color(r, g, b);
    }

    private static int getRandomIntColor() {
        int[] rgb = getRandomRgb();
        int color = 0;
        for (int c : rgb) {
            color = color << 8;
            color = color | c;
        }
        return color;
    }

    private static int[] getRandomRgb() {
        int[] rgb = new int[3];
        for (int i = 0; i < 3; i++) {
            rgb[i] = ThreadLocalRandom.current().nextInt(255);
        }
        return rgb;
    }

    private static void shear(Graphics g, int w1, int h1, Color color) {
        shearX(g, w1, h1, color);
        shearY(g, w1, h1, color);
    }

    private static void shearX(Graphics g, int w1, int h1, Color color) {
        int period = ThreadLocalRandom.current().nextInt(2);

        boolean borderGap = true;
        double frames = 1, phase = ThreadLocalRandom.current().nextInt(2);
        for (int d, i = 0; i < h1; i++) {
            d = (int) ((period >> 1) * Math.sin((double) i / period + (2 * Math.PI * phase) / frames));
            g.copyArea(0, i, w1, 1, d, 0);
            if (borderGap) {
                g.setColor(color);
                g.drawLine(d, i, 0, i);
                g.drawLine(d + w1, i, w1, i);
            }
        }
    }

    private static void shearY(Graphics g, int w1, int h1, Color color) {
        int period = ThreadLocalRandom.current().nextInt(40) + 10; // 50;
        boolean borderGap = true;
        double frames = 20, phase = 7;
        for (int d, i = 0; i < w1; i++) {
            d = (int) ((period >> 1) * Math.sin((double) i / period + (2 * Math.PI * phase) / frames));
            g.copyArea(i, 0, 1, h1, 0, d);
            if (borderGap) {
                g.setColor(color);
                g.drawLine(i, d, i, 0);
                g.drawLine(i, d + h1, i, h1);
            }
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        int width = 80;
        generate(width, (int) (width * 0.618), new FileOutputStream("D:/a.jpg"), random(4));
    }

}
