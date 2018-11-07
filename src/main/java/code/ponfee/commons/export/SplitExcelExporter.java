package code.ponfee.commons.export;

import java.util.concurrent.ExecutorService;

/**
 * Export multiple excel file
 *
 * @author fupf
 */
public class SplitExcelExporter extends AbstractSplitExporter {

    public SplitExcelExporter(int batchSize, String savingFilePathPrefix,
                              ExecutorService executor) {
        super(batchSize, savingFilePathPrefix, executor);
    }

    @Override
    protected AsnycSplitExporter splitExporter(Table subTable, int number) {
        return new AsnycExcelExporter(
            subTable, super.savingFilePathPrefix + number, super.getName()
        );
    }

    private static class AsnycExcelExporter extends AsnycSplitExporter {
        private final String sheetName;

        private AsnycExcelExporter(Table subTable, String savingFilePath, 
                                   String sheetName) {
            super(subTable, savingFilePath, ".xlsx");
            this.sheetName = sheetName;
        }

        @Override
        protected void build() {
            try (ExcelExporter excel = new ExcelExporter()) {
                excel.setName(sheetName);
                excel.build(subTable);
                excel.write(savingFilePath);
            }
        }
    }

}
