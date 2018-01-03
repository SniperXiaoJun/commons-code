package code.ponfee.commons.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.swing.ImageIcon;

import com.google.common.io.Files;

/**
 * 图片工具类
 * @author fupf
 */
public class ImageUtils {

    public static void main(String[] args) throws IOException {
        List<byte[]> list = new ArrayList<>();
        list.add(Files.toByteArray(new File("D:\\imgs\\fox(01-19-16-51-28)(4).png")));
        list.add(Files.toByteArray(new File("D:\\imgs\\fox(01-19-16-51-28)(6).png")));
        list.add(Files.toByteArray(new File("D:\\imgs\\fox(01-19-16-51-28)(7).png")));
        Files.write(mergeVertical(list, "PNG"), new File("d:/out.png"));
    }

    /**
     * 获取图片大小
     * @param input
     * @return [width, height]
     */
    public static int[] getImageSize(InputStream input) {
        try {
            BufferedImage image = ImageIO.read(input);
            return new int[] { image.getWidth(), image.getHeight() };
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (input != null) try {
                input.close();
            } catch (IOException ignored) {
                // ignored
            }
        }
    }

    /**
     * 横向合并图片
     * @param imgs
     * @param format
     * @return
     */
    public static byte[] mergeHorizontal(List<byte[]> imgs, String format) {
        int width = 0, height = 0;
        try {
            List<BufferedImage> list = new ArrayList<BufferedImage>();
            for (byte[] img : imgs) {
                BufferedImage i = ImageIO.read(new ByteArrayInputStream(img));
                width += i.getWidth();// 图片宽度
                height = Math.max(height, i.getHeight());
                list.add(i);
            }

            BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            width = 0;
            for (BufferedImage i : list) {
                int[] array = new int[i.getWidth() * i.getHeight()];// 从图片中读取RGB
                array = i.getRGB(0, 0, i.getWidth(), i.getHeight(), array, 0, i.getWidth());
                result.setRGB(width, 0, i.getWidth(), i.getHeight(), array, 0, i.getWidth());// 设置左半部分的RGB
                width += i.getWidth();// 图片宽度
                i.flush();
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(result, format, out);
            out.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("图片合并失败", e);
        }
    }

    /**
     * 纵向合并图片
     * @param imgs
     * @param format  png,jpeg,gif
     * @return
     */
    public static byte[] mergeVertical(List<byte[]> imgs, String format) {
        try {
            int width = 0, height = 0;
            List<BufferedImage> list = new ArrayList<BufferedImage>();
            for (byte[] img : imgs) {
                BufferedImage i = ImageIO.read(new ByteArrayInputStream(img));
                height += i.getHeight();// 图片宽度
                width = Math.max(width, i.getWidth());
                list.add(i);
            }

            BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            height = 0;
            for (BufferedImage i : list) {
                int[] array = new int[i.getWidth() * i.getHeight()];// 从图片中读取RGB
                array = i.getRGB(0, 0, i.getWidth(), i.getHeight(), array, 0, i.getWidth());
                result.setRGB(0, height, i.getWidth(), i.getHeight(), array, 0, i.getWidth());// 设置左半部分的RGB
                height += i.getHeight();// 图片宽度
                i.flush();
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(result, format, out);
            out.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("图片合并失败", e);
        }
    }

    /**
     * <pre>
     * 图片透明处理
     *     白    rgb:-1-->255,255,255
     *     红    rgb:-65536-->255,0,0
     *   透明    rgb:0-->0,0,0
     *     红    rgb:-922812416-->255,0,0
     *     黑    rgb:-16777216-->0,0,0
     *     黑    rgb:-939524096-->0,0,0
     *  </pre>
     * @param bytes
     * @return
     */
    public static byte[] transparent(byte[] bytes, int refer, int normal) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ImageIcon imageIcon = new ImageIcon(ImageIO.read(new ByteArrayInputStream(bytes)));
            BufferedImage bufferedImage = new BufferedImage(imageIcon.getIconWidth(), imageIcon.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g2D = (Graphics2D) bufferedImage.getGraphics();
            g2D.drawImage(imageIcon.getImage(), 0, 0, imageIcon.getImageObserver());
            for (int alpha, rgb, j, i = bufferedImage.getMinX(); i < bufferedImage.getWidth(); i++) {
                for (j = bufferedImage.getMinY(); j < bufferedImage.getHeight(); j++) {
                    rgb = bufferedImage.getRGB(i, j);
                    if (rgb != 0) { // 0为透明
                        if (compare(rgb, refer)) {
                            alpha = 0; // -1为白色：255 255 255
                        } else {
                            alpha = normal; // 默认设置半透明
                        }
                        rgb = (alpha << 24) | (rgb & 0x00ffffff); // 计算rgb
                        bufferedImage.setRGB(i, j, rgb); // 重新设置rgb
                    }
                }
            }
            //g2D.drawImage(bufferedImage, 0, 0, imageIcon.getImageObserver());
            ImageIO.write(bufferedImage, "png", bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (bos != null) try {
                bos.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * 图片类型
     * @param bytes
     * @return
     * @throws IOException
     */
    public static String getImageType(byte[] bytes) throws IOException {
        MemoryCacheImageInputStream mcis = null;
        try {
            List<String> types = new ArrayList<>();
            mcis = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes));
            for (Iterator<ImageReader> itr = ImageIO.getImageReaders(mcis); itr.hasNext();) {
                types.add(itr.next().getFormatName());
                /*if (reader instanceof com.sun.imageio.plugins.gif.GIFImageReader) {
                    return "gif";
                } else if (reader instanceof com.sun.imageio.plugins.jpeg.JPEGImageReader) {
                    return "jpeg";
                } else if (reader instanceof com.sun.imageio.plugins.png.PNGImageReader) {
                    return "png";
                } else if (reader instanceof com.sun.imageio.plugins.bmp.BMPImageReader) {
                    return "bmp";
                } else if (reader instanceof com.sun.imageio.plugins.wbmp.WBMPImageReader) {
                    return "wbmp";
                }*/
            }
            if (types.size() == 0) {
                return null;
            } else if (types.size() == 1) {
                return types.get(0);
            } else {
                return types.toString();
            }
        } finally {
            if (mcis != null) try {
                mcis.close();
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
        }
    }

    private static boolean compare(int color, int colorRange) {
        int r = (color & 0xff0000) >> 16;
        int g = (color & 0x00ff00) >> 8;
        int b = (color & 0x0000ff);
        return (r >= colorRange && g >= colorRange && b >= colorRange);
    }

}
