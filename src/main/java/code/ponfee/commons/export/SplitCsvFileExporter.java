package code.ponfee.commons.export;

import java.util.concurrent.ExecutorService;

/**
 * Export multiple excel file
 *
 * @author fupf
 */
public class SplitCsvFileExporter extends AbstractSplitExporter {

    private final boolean withBom;

    public SplitCsvFileExporter(int batchSize, String savingFilePathPrefix,
                                boolean withBom, ExecutorService executor) {
        super(batchSize, savingFilePathPrefix, executor);
        this.withBom = withBom;
    }

    @Override
    protected AsnycSplitExporter splitExporter(Table subTable, int number) {
        return new AsnycCsvFileExporter(
            subTable, super.savingFilePathPrefix + number, withBom
        );
    }

    private static class AsnycCsvFileExporter extends AsnycSplitExporter {
        private final boolean withBom;

        private AsnycCsvFileExporter(Table subTable, String savingFilePath, 
                                     boolean withBom) {
            super(subTable, savingFilePath, ".csv");
            this.withBom = withBom;
        }

        @Override
        protected void build() {
            try (CsvFileExporter csv = new CsvFileExporter(savingFilePath, withBom)) {
                csv.build(subTable);
            }
        }
    }

}
