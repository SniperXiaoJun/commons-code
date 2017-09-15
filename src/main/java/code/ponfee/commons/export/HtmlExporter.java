package code.ponfee.commons.export;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import code.ponfee.commons.export.Tmeta.Type;
import code.ponfee.commons.util.ExtendedMessageFormat;
import code.ponfee.commons.util.Numbers;
import code.ponfee.commons.util.ObjectUtils;

/**
 * html导出
 * @author fupf
 */
public class HtmlExporter extends AbstractExporter {
    //private static final Pattern PATTERN_NEGATIVE = Pattern.compile("^(-(([0-9]+\\.[0-9]*[1-9][0-9]*)|([0-9]*[1-9][0-9]*\\.[0-9]+)|([0-9]*[1-9][0-9]*)))(%)?$");
    private static final String HORIZON = "<hr style=\"border:3 double #b0c4de; with: 95%; margin: 20px 0;\" />";
    private static final String TITLE = "title";
    private static final String REPORT = "report";
    private static final String TEMPLATE;
    static {
        StringBuilder builder = new StringBuilder(4096);
        builder.append("<!DOCTYPE html>                                                                                                       \n");
        builder.append("<html>                                                                                                                \n");
        builder.append("  <head lang=\"en\">                                                                                                  \n");
        builder.append("    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />                                         \n");
        builder.append("    <title>#{" + TITLE + "}</title>                                                                                   \n");
        builder.append("    <style>                                                                                                           \n");
        builder.append("      * '{font-family: Microsoft YaHei;}'                                                                             \n");
        builder.append("      div.grid '{overflow-x: auto;background-color: #fff;color: #555;}'                                               \n");
        builder.append("      div.grid table '{width: 100%;font-size: 12px;border-collapse: collapse;border-style: hidden;}'                  \n");
        builder.append("      div.grid table, div.grid table caption, div.grid table tr '{border: 1px solid #6d6d6d;}'                        \n");
        builder.append("      div.grid table tr td, div.grid table tr th '{border: 1px solid #6d6d6d;}'                                       \n");
        builder.append("      div.grid table caption '{font-size:14px;padding:5px;background:#e6e6fa;font-weight:bolder;border-bottom:none;}' \n");
        builder.append("      div.grid table thead th '{padding: 5px;background: #ccc;}'                                                      \n");
        builder.append("      div.grid table tbody td '{text-align: center;padding: 3px;}'                                                    \n");
        builder.append("      div.grid table tfoot th '{padding: 5px;}'                                                                       \n");
        builder.append("      div.grid table tr:nth-child(odd) td'{background:#fff;}'                                                         \n");
        builder.append("      div.grid table tr:nth-child(even) td'{background: #e8e8e8}'                                                     \n");
        builder.append("      div.grid p.remark '{font-size: 14px;}'                                                                          \n");
        builder.append("      div.grid .nowrap '{white-space: nowrap;word-break: keep-all;overflow: hidden;text-overflow: ellipsis;}'         \n");
        builder.append("    </style>                                                                                                          \n");
        builder.append("  </head>                                                                                                             \n");
        builder.append("  <body>#{" + REPORT + "}</body>                                                                                      \n");
        builder.append("</html>                                                                                                               \n");
        TEMPLATE = builder.toString();
        builder.setLength(0);
    }

    private StringBuilder html;

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
        if (ObjectUtils.isEmpty(table.getThead())) {
            throw new IllegalArgumentException("thead can't be null");
        }

        horizon();
        html.append("<div class=\"grid\"><table cellpadding=\"0\" cellspacing=\"0\">");
        if (StringUtils.isNotBlank(table.getCaption())) {
            html.append("<caption>" + table.getCaption() + "</caption>");
        }

        // thead
        buildComplexThead(table.getThead(), table.getMaxTheadLevel());

        if (ObjectUtils.isEmpty(table.getTobdy()) && ObjectUtils.isEmpty(table.getTfoot())) {
            html.append("<tfoot><tr><td colspan=\"").append(table.getTotalLeafCount());
            html.append("\" style=\"color:red; padding: 3px;font-size: 14px;\">");
            html.append(TIP_NO_RESULT).append("</td></tr></tfoot>");
        } else {
            super.nonEmpty();
            // tbody
            if (!ObjectUtils.isEmpty(table.getTobdy())) {
                html.append("<tbody>");
                Object[] datas;
                for (int n = table.getTobdy().size(), i = 0; i < n; i++) {
                    datas = table.getTobdy().get(i);
                    html.append("<tr>");
                    for (int m = datas.length, j = 0; j < m; j++) {
                        html.append("<td");

                        processMeta(datas[j], table.getThead().get(j).getTmeta(), i, j, table.getOptions());

                        html.append(">").append(formatData(datas[j], table.getThead().get(j).getTmeta())).append("</td>");
                    }
                    html.append("</tr>");
                }
                html.append("</tbody>");
            }

            // tfoot---------
            boolean hasTfoot = false;
            if (!ObjectUtils.isEmpty(table.getTfoot())) {
                hasTfoot = true;
                html.append("<tfoot><tr>");
                int merge = table.getTotalLeafCount() - table.getTfoot().length;
                if (merge > 0) html.append("<th colspan=\"" + merge + "\" style=\"text-align:right;\">合计</th>");

                for (int i = 0; i < table.getTfoot().length; i++) {
                    html.append("<th");

                    processMeta(table.getTfoot()[i], table.getThead().get(merge + i).getTmeta());

                    html.append(">");
                    html.append(formatData(table.getTfoot()[i], table.getThead().get(merge + i).getTmeta()));
                    html.append("</th>");
                }
                html.append("</tr></tfoot>");
            }

            // comment------
            if (StringUtils.isNotBlank(table.getComment())) {
                StringBuilder builder = new StringBuilder();
                String[] comments = table.getComment().split(";");
                builder.append("<tr><td colspan=\"").append(table.getTotalLeafCount());
                builder.append("\" style=\"color:red; padding: 3px;font-size: 14px;\">");
                builder.append("<div style=\"font-weight: bold;\">备注：</div>");
                for (String comment : comments) {
                    builder.append("<div style=\"text-indent: 2em;\">").append(comment).append("</div>");
                }
                builder.append("</td></tr>");

                if (hasTfoot) {
                    html.insert(html.length() - "</tfoot>".length(), builder);
                } else {
                    html.append("<tfoot>").append(builder).append("</tfoot>");
                }
            }
        }

        // end-----
        html.append("</table></div>");
    }

    @Override
    public String export() {
        return ExtendedMessageFormat.formatPair(TEMPLATE, TITLE, super.getName(), REPORT, html.toString());
    }

    @Override
    public void close() {
        html.setLength(0);
        html = null;
    }

    public HtmlExporter horizon() {
        if (html.length() > 0) {
            html.append(HORIZON);
        }
        return this;
    }

    // htmlExporter.horizon().append("<div align=\"center\"><img src=\"cid:" + chart.getId() + "\" /></div>");
    public HtmlExporter append(String string) {
        if (StringUtils.isNotBlank(string)) {
            super.nonEmpty();
            html.append(string);
        }
        return this;
    }

    // 简单表头
    /*private void buildSimpleThead(String[] theadName) {
        html.append("<thead><tr>");
        for (String th : theadName) {
            html.append("<th>").append(th).append("</th>");
        }
        html.append("</tr></thead>");
    }*/

    // 复合表头
    private void buildComplexThead(List<Thead> thead, int maxTheadLevel) {
        html.append("<thead><tr>");
        int level = 1;
        for (Thead cell : thead) {
            if (level < cell.getNodeLevel()) {
                html.append("</tr><tr>");
                level = cell.getNodeLevel();
            }
            html.append("<th");
            if (cell.isLeaf()) {
                // 叶子节点，跨行
                if (maxTheadLevel - cell.getNodeLevel() > 0) {
                    html.append(" rowspan=\"" + (maxTheadLevel - cell.getNodeLevel() + 1) + "\"");
                }
            } else {
                // 非叶子节点，跨列
                if (cell.getChildLeafCount() > 1) {
                    html.append(" colspan=\"" + cell.getChildLeafCount() + "\"");
                }
            }
            html.append(">").append(cell.getName()).append("</th>");
        }
        html.append("</tr></thead>");
    }

    private String formatData(Object obj, Tmeta tmeta) {
        if (ObjectUtils.isEmpty(obj)) {
            return "";
        } else if (tmeta == null) {
            return obj.toString();
        } else if (tmeta.getType() == Type.NUMERIC) {
            return Numbers.format(obj);
        } else {
            return obj.toString();
        }
    }

    private void processMeta(Object value, Tmeta tmeta) {
        processMeta(value, tmeta, -1, -1, null);
    }

    @SuppressWarnings("unchecked")
    private void processMeta(Object value, Tmeta tmeta, int row, int col, Map<String, Object> options) {
        StringBuffer style = new StringBuffer();
        List<String> css = new ArrayList<>();

        /*if (PATTERN_NEGATIVE.matcher(Objects.toString(value, "")).matches()) {
            style.append("color:#006400;font-weight:bold;"); // 负数显示绿色
        }*/

        if (tmeta != null) {
            switch (tmeta.getAlign()) {
                case LEFT:
                    style.append("text-align:left;");
                    break;
                case CENTER:
                    style.append("text-align:right;");
                    break;
                case RIGHT:
                    style.append("text-align:center;");
                    break;
                default:
                    break;
            }

            if (tmeta.getColor() != null) {
                style.append("color: ").append(tmeta.getColorHex()).append(";");
            }

            if (tmeta.isNowrap()) {
                css.add("nowrap");
            }
        }

        if (!ObjectUtils.isEmpty(options)) {
            if (row >= 0 && col >= 0 && !ObjectUtils.isEmpty(options.get("highlight"))) {
                Map<String, Object> highlight = (Map<String, Object>) options.get("highlight");
                String color = "color: " + highlight.get("color") + ";font-weight: bold;";
                List<List<Integer>> cells = (List<List<Integer>>) highlight.get("cells");
                for (List<Integer> cell : cells) {
                    if (cell.get(0).equals(row) && cell.get(1).equals(col)) {
                        style.append(color);
                    }
                }
            }
        }

        if (style.length() > 0) {
            html.append(" style=\"").append(style.toString()).append("\"");
        }
        if (!css.isEmpty()) {
            html.append(" class=\"").append(StringUtils.join(css.toArray(), " ")).append("\"");
        }
    }

}
