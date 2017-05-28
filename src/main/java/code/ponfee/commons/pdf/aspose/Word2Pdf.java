package code.ponfee.commons.pdf.aspose;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.aspose.words.Document;
import com.aspose.words.FontSettings;
import com.aspose.words.FontSourceBase;
import com.aspose.words.License;
import com.aspose.words.LineStyle;
import com.aspose.words.MemoryFontSource;
import com.aspose.words.NodeType;
import com.aspose.words.PdfSaveOptions;
import com.aspose.words.Table;

import code.ponfee.commons.resource.Resource;
import code.ponfee.commons.resource.ResourceLoaderFacade;
import code.ponfee.commons.util.Streams;

/**
 * 依赖
 * <dependency>
 *   <groupId>aspose</groupId>
 *   <artifactId>words-jdk16</artifactId>
 *   <version>1.0.0</version>
 *   <scope>system</scope>
 *   <systemPath>${project.basedir}/lib/aspose.words.jdk16.jar</systemPath>
 * </dependency>
 * word转pdf
 * @author fupf
 */
public final class Word2Pdf {

    private final static FontSourceBase[] FONTS;
    static {
        // 加载字体
        List<Resource> list = ResourceLoaderFacade.listResources(new String[] { "ttf" }, Word2Pdf.class);
        FONTS = new FontSourceBase[list.size()];
        for (int n = list.size(), i = 0; i < n; i++) {
            try {
                FONTS[i] = new MemoryFontSource(Streams.input2bytes(list.get(i).getStream()));
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
        }

        // 加载license
        InputStream input = null;
        try {
            input = ResourceLoaderFacade.getResource("license.xml", Word2Pdf.class).getStream();
            new License().setLicense(input);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        } finally {
            if (input != null) try {
                input.close();
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
        }
    }

    /**
     * word转pdf
     * @param words
     * @param out
     */
    public static void convert(InputStream words, OutputStream out) {
        try {
            Document doc = new Document(words);
            FontSettings.setFontsSources(FONTS);
            PdfSaveOptions pso = new PdfSaveOptions();
            pso.setEmbedFullFonts(false);

            Table table = (Table) doc.getChild(NodeType.TABLE, 1, true);
            if (table != null) {
                table.setBorders(LineStyle.SINGLE, 1.0, Color.BLACK);
            }
            doc.save(out, pso);
            out.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(Streams.file2bytes("d:/test/CDR中关于对象选择.doc"));
        convert(input, new FileOutputStream("d:/test/testpdf.pdf"));
    }
}
