package code.ponfee.commons.export;

import java.io.IOException;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import code.ponfee.commons.io.Files;
import code.ponfee.commons.tree.FlatNode;

/**
 * csv导出
 * @author fupf
 */
public abstract class AbstractCsvExporter<T> extends AbstractExporter<T> {

    static final byte[] WINDOWS_BOM = {
        (byte) 0xEF, (byte) 0xBB, (byte) 0xBF
    };

    protected final Appendable csv;
    private final char csvSeparator;
    private volatile boolean hasBuild = false;

    public AbstractCsvExporter(Appendable csv) {
        this(csv, ',');
    }

    public AbstractCsvExporter(Appendable csv, char csvSeparator) {
        this.csv = csv;
        this.csvSeparator = csvSeparator;
    }

    @Override
    public final void build(Table table) {
        if (!hasBuild) {
            synchronized (this) {
                if (hasBuild) {
                    throw new UnsupportedOperationException("Only support signle table.");
                }
                hasBuild = true;
            }
        }

        List<FlatNode<Integer>> thead = table.getThead();
        if (CollectionUtils.isEmpty(thead)) {
            throw new IllegalArgumentException("Thead cannot be null.");
        }

        // build table thead
        buildComplexThead(thead);

        // tbody---------------
        rollingTbody(table, (data, i) -> {
            try {
                for (int m = data.length - 1, j = 0; j <= m; j++) {
                    csv.append(String.valueOf(data[j]));
                    if (j < m) {
                        csv.append(csvSeparator);
                    }
                }
                csv.append(Files.SYSTEM_LINE_SEPARATOR); // 换行
                //if ((i & 0xFF) == 0) {
                //    this.flush();
                //}
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        try {
            if (table.isEmptyTbody()) {
                csv.append(NO_RESULT_TIP);
            } else {
                super.nonEmpty();
            }

            // tfoot---------
            if (ArrayUtils.isNotEmpty(table.getTfoot())) {
                FlatNode<Integer> root = thead.get(0);
                if (table.getTfoot().length > root.getChildLeafCount()) {
                    throw new IllegalStateException("Tfoot length cannot more than total leaf count.");
                }

                int n = root.getChildLeafCount(), m = table.getTfoot().length, mergeNum = n - m;
                for (int i = 0; i < mergeNum; i++) {
                    if (i == mergeNum - 1) {
                        csv.append("合计");
                    }
                    csv.append(csvSeparator);
                }
                for (int i = mergeNum; i < n; i++) {
                    csv.append(String.valueOf(table.getTfoot()[i - mergeNum]));
                    if (i != n - 1) {
                        csv.append(csvSeparator);
                    }
                }

                csv.append(Files.SYSTEM_LINE_SEPARATOR);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void flush() {}

    private void buildComplexThead(List<FlatNode<Integer>> thead) {
        List<FlatNode<Integer>> leafs = super.getLeafThead(thead);
        try {
            for (int i = 0, n = leafs.size(); i < n; i++) {
                FlatNode<Integer> th = leafs.get(i);
                csv.append(((Thead) th.getAttach()).getName());
                if (i != n - 1) {
                    csv.append(csvSeparator);
                }
            }
            csv.append(Files.SYSTEM_LINE_SEPARATOR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 创建简单表头
    /*private void buildSimpleThead(String[] theadName) {
        for (String th : theadName) {
            csv.append(th).append(csvSeparator);
        }
        csv.deleteCharAt(csv.length() - 1);
        csv.append(Files.LINE_SEPARATOR);
    }*/
}
