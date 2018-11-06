package code.ponfee.commons.export;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import code.ponfee.commons.io.Files;
import code.ponfee.commons.io.WrappedBufferedWriter;
import code.ponfee.commons.tree.FlatNode;

/**
 * csv导出
 * @author fupf
 */
public class CsvFileExporter extends AbstractExporter {

    private static final byte[] WINDOWS_BOM = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
    private final WrappedBufferedWriter writer;
    private final char csvSeparator;

    public CsvFileExporter(String filePath, boolean withBom) {
        this(new File(filePath), StandardCharsets.UTF_8, withBom);
    }

    public CsvFileExporter(File file, Charset charset, boolean withBom) {
        try {
            writer = new WrappedBufferedWriter(file, charset);
            if (withBom) {
                writer.write(WINDOWS_BOM);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.csvSeparator = ',';
    }

    @Override
    public void build(Table table) {
        List<FlatNode<Integer>> thead = table.getThead();
        if (thead == null || thead.isEmpty()) {
            throw new IllegalArgumentException("thead can't be null");
        }

        // build table thead
        buildComplexThead(thead);

        // tbody---------------
        rollingTbody(table, (data, i) -> {
            try {
                for (int m = data.length - 1, j = 0; j <= m; j++) {
                    writer.append(String.valueOf(data[j]));
                    if (j < m) {
                        writer.append(csvSeparator);
                    }
                }
                writer.append(Files.SYSTEM_LINE_SEPARATOR); // 换行
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        try {
            if (table.isEmptyTbody()) {
                writer.append(NO_RESULT_TIP);
            } else {
                super.nonEmpty();
            }

            // tfoot---------
            if (ArrayUtils.isNotEmpty(table.getTfoot())) {
                FlatNode<Integer> root = thead.get(0);
                if (table.getTfoot().length > root.getChildLeafCount()) {
                    throw new IllegalStateException("tfoot data length cannot more than total leaf count.");
                }

                int n = root.getChildLeafCount(), m = table.getTfoot().length, mergeNum = n - m;
                for (int i = 0; i < mergeNum; i++) {
                    if (i == mergeNum - 1) {
                        writer.append("合计");
                    }
                    writer.append(csvSeparator);
                }
                for (int i = mergeNum; i < n; i++) {
                    writer.append(String.valueOf(table.getTfoot()[i - mergeNum]));
                    if (i != n - 1) {
                        writer.append(csvSeparator);
                    }
                }

                writer.append(Files.SYSTEM_LINE_SEPARATOR);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String export() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        writer.close();
    }

    private void buildComplexThead(List<FlatNode<Integer>> thead) {
        List<FlatNode<Integer>> leafs = super.getLeafThead(thead);
        try {
            for (int i = 0, n = leafs.size(); i < n; i++) {
                FlatNode<Integer> th = leafs.get(i);
                writer.append(((Thead) th.getAttach()).getName());
                if (i != n - 1) {
                    writer.append(csvSeparator);
                }
            }
            writer.append(Files.SYSTEM_LINE_SEPARATOR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
