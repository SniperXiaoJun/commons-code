package code.ponfee.commons.export;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import code.ponfee.commons.io.Files;
import code.ponfee.commons.tree.FlatNode;

/**
 * csv导出
 * @author fupf
 */
public class CsvExporter extends AbstractExporter {

    private StringBuilder csv;
    private final char csvSeparator;

    public CsvExporter() {
        this(',');
    }

    public CsvExporter(char csvSeparator) {
        this.csvSeparator = csvSeparator;
        this.csv = new StringBuilder(0x2000); // 初始容量8192
    }

    @Override
    public void build(Table table) {
        if (csv.length() > 0) {
            throw new UnsupportedOperationException("only support signle table");
        }

        List<FlatNode<Integer>> flats = table.getThead();
        if (flats == null || flats.isEmpty()) {
            throw new IllegalArgumentException("thead can't be null");
        }

        // build table thead
        buildComplexThead(flats);

        // tbody---------------
        rollingTbody(table, (data, i) -> {
            for (int m = data.length - 1, j = 0; j <= m; j++) {
                csv.append(data[j]);
                if (j < m) {
                    csv.append(csvSeparator);
                }
            }
            csv.append(Files.SYSTEM_LINE_SEPARATOR); // 换行
        });

        if (table.isEmptyTbody()) {
            csv.append(NO_RESULT_TIP);
        } else {
            super.nonEmpty();
        }

        // tfoot---------
        if (ArrayUtils.isNotEmpty(table.getTfoot())) {
            FlatNode<Integer> root = flats.get(0);
            if (table.getTfoot().length > root.getChildLeafCount()) {
                throw new IllegalStateException("tfoot data length cannot more than total leaf count.");
            }

            int n = root.getChildLeafCount(), m = table.getTfoot().length, mergeNum = n - m;
            for (int i = 0; i < mergeNum; i++) {
                if (i == mergeNum - 1) {
                    csv.append("合计");
                }
                csv.append(csvSeparator);
            }
            for (int i = mergeNum; i < n; i++) {
                csv.append(table.getTfoot()[i - mergeNum]);
                if (i != n - 1) {
                    csv.append(csvSeparator);
                }
            }

            csv.append(Files.SYSTEM_LINE_SEPARATOR);
        }
    }

    @Override
    public Object export() {
        return csv.toString();
    }

    @Override
    public void close() {
        csv = null;
    }

    private void buildComplexThead(List<FlatNode<Integer>> flats) {
        for (FlatNode<Integer> cell : flats.subList(1, flats.size())) {
            if (cell.isLeaf()) {
                csv.append(((Thead) cell.getAttach()).getName()).append(csvSeparator);
            }
        }
        csv.deleteCharAt(csv.length() - 1);
        csv.append(Files.SYSTEM_LINE_SEPARATOR);
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
