package code.ponfee.commons.export;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import code.ponfee.commons.concurrent.MultithreadExecutor;
import code.ponfee.commons.util.Holder;

/**
 * Export multiple excel file
 *
 * @author fupf
 */
public class SplitExcelExporter extends AbstractExporter {

    private final int batchSize;
    private final String savingFilePathPrefix;
    private final ExecutorService executor;

    public SplitExcelExporter(int batchSize, String savingFilePathPrefix, 
                              ExecutorService executor) {
        this.batchSize = batchSize;
        this.savingFilePathPrefix = savingFilePathPrefix;
        this.executor = executor;
    }

    /**
     * 构建excel
     */
    @Override
    public void build(Table table) {
        CompletionService<Void> service = new ExecutorCompletionService<>(executor);
        AtomicInteger count = new AtomicInteger(0);
        AtomicInteger split = new AtomicInteger(0);
        Holder<Table> subTable = Holder.of(table.copyOfWithoutTbody());
        rollingTbody(table, (data, i) -> {
            subTable.get().addRow(data);
            if (count.incrementAndGet() == batchSize) {
                super.nonEmpty();
                Table sub = subTable.set(table.copyOfWithoutTbody());
                count.set(0); // reset count and sub table
                service.submit(new AsnycExcelExporter(
                    this.getName(), sub, this.savingFilePathPrefix + split.incrementAndGet()
                ));
            }
        });
        if (!subTable.get().isEmptyTbody()) {
            super.nonEmpty();
            service.submit(new AsnycExcelExporter(
                this.getName(), subTable.get(), 
                this.savingFilePathPrefix + split.incrementAndGet()
            ));
        }

        MultithreadExecutor.joinDiscard(service, split.get(), AWAIT_TIME_MILLIS);
    }

    /**
     * 导出
     */
    @Override
    public byte[] export() {
        throw new UnsupportedOperationException();
    }

    /**
     * 关闭
     */
    @Override
    public void close() {}

    private static class AsnycExcelExporter implements Callable<Void> {
        private final String sheetName;
        private final Table table;
        private final String savingFilePath;

        private AsnycExcelExporter(String sheetName, Table table, 
                                   String savingFilePath) {
            this.sheetName = sheetName;
            this.table = table;
            this.savingFilePath = savingFilePath + ".xlsx";
        }

        @Override
        public Void call() throws Exception {
            table.end();
            try (ExcelExporter excel = new ExcelExporter()) {
                excel.setName(sheetName);
                excel.build(table);
                excel.write(savingFilePath);
            }
            return null;
        }
    }

}
