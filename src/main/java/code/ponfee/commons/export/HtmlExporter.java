package code.ponfee.commons.export;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import code.ponfee.commons.export.Tmeta.Type;
import code.ponfee.commons.math.Numbers;
import code.ponfee.commons.tree.FlatNode;

/**
 * html导出
 * @author fupf
 */
public class HtmlExporter extends AbstractExporter {

    //private static final Pattern PATTERN_NEGATIVE = Pattern.compile("^(-(([0-9]+\\.[0-9]*[1-9][0-9]*)|([0-9]*[1-9][0-9]*\\.[0-9]+)|([0-9]*[1-9][0-9]*)))(%)?$");

    private static final String HORIZON = "<hr style=\"border:3 double #b0c4de;with:95%;margin:20px 0;\" />";
    private static final String TEMPLATE = new StringBuilder(4096) 
       .append("<!DOCTYPE html>                                                                                                    \n")
       .append("<html>                                                                                                             \n")
       .append("  <head lang=\"en\">                                                                                               \n")
       .append("    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />                                      \n")
       .append("    <title>{0}</title>                                                                                             \n")
       .append("    <style>                                                                                                        \n")
       .append("      * '{font-family: Microsoft YaHei;}'                                                                          \n")
       .append("      .grid '{overflow-x: auto;background-color: #fff;color: #555;}'                                               \n")
       .append("      .grid table '{width: 100%;font-size: 12px;border-collapse: collapse;border-style: hidden;}'                  \n")
       .append("      .grid table, div.grid table caption, div.grid table tr '{border: 1px solid #6d6d6d;}'                        \n")
       .append("      .grid table tr td, div.grid table tr th '{border: 1px solid #6d6d6d;}'                                       \n")
       .append("      .grid table caption '{font-size:14px;padding:5px;background:#e6e6fa;font-weight:bolder;border-bottom:none;}' \n")
       .append("      .grid table thead th '{padding: 5px;background: #ccc;}'                                                      \n")
       .append("      .grid table tbody td '{text-align: center;padding: 3px;}'                                                    \n")
       .append("      .grid table tbody td.text-left '{text-align:left;}'                                                          \n")
       .append("      .grid table tbody td.text-right '{text-align:right;}'                                                        \n")
       .append("      .grid table tbody td.text-center '{text-align:center;}'                                                      \n")
       .append("      .grid table tfoot th '{padding: 5px;}'                                                                       \n")
       .append("      .grid table tr:nth-child(odd) td'{background:#fff;}'                                                         \n")
       .append("      .grid table tr:nth-child(even) td'{background: #e8e8e8}'                                                     \n")
       .append("      .grid p.remark '{font-size: 14px;}'                                                                          \n")
       .append("      .grid .nowrap '{white-space: nowrap;word-break: keep-all;overflow: hidden;text-overflow: ellipsis;}'         \n")
       .append("    </style>                                                                                                       \n")
       .append("  </head>                                                                                                          \n")
       .append("  <body>{1}</body>                                                                                                 \n")
       .append("</html>                                                                                                            \n")
       .toString();

    private StringBuilder html; // StringBuilder扩容：(value.length << 1) + 2
                                // 容量如果不够，直接扩充到需要的容量大小

    public HtmlExporter() {
        this.html = new StringBuilder(0x2000); // 初始容量8192
    }

    public HtmlExporter(String initHtml) {
        this.html = new StringBuilder(initHtml);
    }

    /**
     * 构建html
     */
    @Override
    public void build(Table table) {
        List<FlatNode<Integer>> flats = table.getThead();
        if (flats == null || flats.isEmpty()) {
            throw new IllegalArgumentException("thead can't be null");
        }

        horizon();
        html.append("<div class=\"grid\"><table cellpadding=\"0\" cellspacing=\"0\">");
        if (StringUtils.isNotBlank(table.getCaption())) {
            html.append("<caption>")
                .append(table.getCaption())
                .append("</caption>");
        }

        // thead
        buildComplexThead(flats);

        // tbody-----------
        List<FlatNode<Integer>> thead = flats.subList(1, flats.size());
        int totalLeafCount = flats.get(0).getChildLeafCount();
        List<Object[]> tbody = table.getTobdy();
        html.append("<tbody>");
        if (CollectionUtils.isNotEmpty(tbody)) {
            Object[] data;
            for (int n = tbody.size(), i = 0, m, j; i < n; i++) {
                data = tbody.get(i);
                html.append("<tr>");
                for (m = data.length, j = 0; j < m; j++) {
                    html.append("<td");

                    processMeta(data[j], tmeta(thead, j), i, j, table.getOptions()); // 样式

                    html.append(">").append(formatData(data[j], tmeta(thead, j))).append("</td>");
                }
                html.append("</tr>");
            }
            super.nonEmpty();
        } else {
            html.append("<tr><td colspan=\"")
                .append(totalLeafCount)
                .append("\" style=\"color:red;padding:3px;font-size:14px;\">")
                .append(NO_RESULT_TIP)
                .append("</td></tr>");
        }
        html.append("</tbody>");

        // tfoot---------
        boolean hasTfoot = false;
        if (ArrayUtils.isNotEmpty(table.getTfoot())) {
            hasTfoot = true;
            html.append("<tfoot><tr>");

            if (table.getTfoot().length > totalLeafCount) {
                throw new IllegalStateException("tfoot data length cannot more than total leaf count.");
            }

            int merge = totalLeafCount - table.getTfoot().length;
            if (merge > 0) {
                html.append("<th colspan=\"")
                    .append(merge)
                    .append("\" style=\"text-align:right;\">合计</th>");
            }

            for (int i = 0; i < table.getTfoot().length; i++) {
                html.append("<th");

                processMeta(table.getTfoot()[i], tmeta(thead, merge + i));

                html.append(">")
                    .append(formatData(table.getTfoot()[i], tmeta(thead, merge + i)))
                    .append("</th>");
            }
            html.append("</tr></tfoot>");
        }

        // comment------
        if (StringUtils.isNotBlank(table.getComment())) {
            StringBuilder builder = new StringBuilder();
            String[] comments = table.getComment().split(";");
            builder.append("<tr><td colspan=\"").append(totalLeafCount);
            builder.append("\" style=\"color:red; padding:3px;font-size:14px;\">");
            builder.append("<div style=\"font-weight:bold;\">备注：</div>");
            for (String comment : comments) {
                builder.append("<div style=\"text-indent:2em;\">").append(comment).append("</div>");
            }
            builder.append("</td></tr>");

            if (hasTfoot) {
                html.insert(html.length() - "</tfoot>".length(), builder);
            } else {
                html.append("<tfoot>").append(builder).append("</tfoot>");
            }
        }

        // end-----
        html.append("</table></div>");
    }

    @Override
    public String export() {
        return MessageFormat.format(TEMPLATE, super.getName(), html.toString());
        //return html.insert(0, "before").append("after").toString();
    }

    @Override
    public void close() {
        html = null;
    }

    public HtmlExporter horizon() {
        if (html.length() > 0) {
            html.append(HORIZON);
        }
        return this;
    }

    //htmlExporter.horizon().append("<div align=\"center\"><img src=\"cid:")
    //                      .append(img.getId()).apend("\" /></div>");
    public HtmlExporter append(String string) {
        if (StringUtils.isNotBlank(string)) {
            super.nonEmpty();
            html.append(string);
        }
        return this;
    }

    // 创建简单表头
    /*private void buildSimpleThead(String[] theadName) {
        html.append("<thead><tr>");
        for (String th : theadName) {
            html.append("<th>").append(th).append("</th>");
        }
        html.append("</tr></thead>");
    }*/

    // 复合表头
    private void buildComplexThead(List<FlatNode<Integer>> flats) {
        html.append("<thead><tr>");
        int lastLevel = 1, treeMaxDepth = flats.get(0).getTreeMaxDepth() - 1, cellLevel;
        for (FlatNode<Integer> flat : flats.subList(1, flats.size())) {
            cellLevel = flat.getLevel() - 1;
            if (lastLevel < cellLevel) {
                html.append("</tr><tr>");
                lastLevel = cellLevel;
            }
            html.append("<th");
            if (flat.isLeaf()) { // 叶子节点，跨行

                if (treeMaxDepth - cellLevel > 0) {
                    html.append(" rowspan=\"").append(treeMaxDepth - cellLevel + 1).append("\"");
                }
            } else { // 非叶子节点，跨列
                if (flat.getChildLeafCount() > 1) {
                    html.append(" colspan=\"").append(flat.getChildLeafCount()).append("\"");
                }
            }
            html.append(">").append(((Thead) flat.getAttach()).getName()).append("</th>");
        }
        html.append("</tr></thead>");
    }

    private Tmeta tmeta(List<FlatNode<Integer>> thead, int index) {
        return ((Thead) thead.get(index).getAttach()).getTmeta();
    }

    private void processMeta(Object value, Tmeta tmeta) {
        processMeta(value, tmeta, -1, -1, null);
    }

    /**
     * 样式处理
     * @param value
     * @param tmeta
     * @param tbodyRowIdx
     * @param tbodyColIdx
     * @param options
     */
    private final StringBuilder style = new StringBuilder();
    private final StringBuilder clazz = new StringBuilder();
    private void processMeta(Object value, Tmeta tmeta, int tbodyRowIdx, 
                             int tbodyColIdx, Map<CellStyleOptions, Object> options) {
        style.setLength(0);
        clazz.setLength(0);

        /*if (PATTERN_NEGATIVE.matcher(Objects.toString(value, "")).matches()) {
            style.append("color:#006400;font-weight:bold;"); // 负数显示绿色
        }*/

        if (tmeta != null) {
            switch (tmeta.getAlign()) {
                case LEFT:
                    clazz.append("text-left ");
                    break;
                case CENTER:
                    clazz.append("text-center ");
                    break;
                case RIGHT:
                    clazz.append("text-right ");
                    break;
                default:
                    break;
            }

            if (tmeta.getColor() != null) {
                style.append("color:").append(tmeta.getColorHex()).append(";");
            }

            if (tmeta.isNowrap()) {
                clazz.append("nowrap ");
            }
        }

        processOptions(style, tbodyRowIdx, tbodyColIdx, options);

        if (style.length() > 0) {
            html.append(" style=\"").append(style.toString()).append("\"");
        }
        if (clazz.length() > 0) {
            html.append(" class=\"").append(clazz.deleteCharAt(clazz.length() - 1)).append("\"");
        }
    }

    /**
     * 格式化
     * @param data
     * @param tmeta
     * @return
     */
    private static String formatData(Object data, Tmeta tmeta) {
        if (data == null) {
            return "";
        } else if (tmeta == null) {
            return data.toString();
        } else if (tmeta.getType() == Type.NUMERIC) {
            return Numbers.format(data);
        } else {
            return data.toString();
        }
    }

    /**
     * 样式自定义处理
     * @param style
     * @param dataRowIdx
     * @param dataColIdx
     * @param options
     */
    @SuppressWarnings("unchecked")
    private static void processOptions(StringBuilder style, int dataRowIdx, int dataColIdx, 
                                       Map<CellStyleOptions, Object> options) {
        if (options == null || options.isEmpty()) {
            return;
        }

        Map<String, Object> highlight = (Map<String, Object>) options.get(CellStyleOptions.HIGHLIGHT);
        if (highlight != null && !highlight.isEmpty()) {
            String color = "color:" + highlight.get("color") + ";font-weight:bold;";
            List<List<Integer>> cells = (List<List<Integer>>) highlight.get("cells");
            for (List<Integer> cell : cells) {
                if (cell.get(0).equals(dataRowIdx) && cell.get(1).equals(dataColIdx)) {
                    style.append(color);
                }
            }
        }

        Function<Object, String> processor = (Function<Object, String>) options.get(CellStyleOptions.CELL_PROCESS);
        if (processor != null) {
            style.append(processor.apply(new Object[] { dataRowIdx, dataColIdx }));
        }
    }

}
