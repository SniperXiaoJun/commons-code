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
public class SplitCsvFileExporter extends AbstractExporter {

    private final int batchSize;
    private final String savingFilePathPrefix;
    private final boolean withBom;
    private final ExecutorService executor;

    public SplitCsvFileExporter(int batchSize, String savingFilePathPrefix, 
                                boolean withBom, ExecutorService executor) {
        this.batchSize = batchSize;
        this.savingFilePathPrefix = savingFilePathPrefix;
        this.withBom = withBom;
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
                service.submit(new AsnycCsvFileExporter(
                    sub, withBom, 
                    this.savingFilePathPrefix + split.incrementAndGet()
                ));
            }
        });
        if (!subTable.get().isEmptyTbody()) {
            super.nonEmpty();
            service.submit(new AsnycCsvFileExporter(
                subTable.get(), withBom,
                this.savingFilePathPrefix + split.incrementAndGet()
            ));
        }

        MultithreadExecutor.joinDiscard(service, split.get(), AWAIT_TIME_MILLIS);
    }

    /**
     * 导出
     */
    @Override
    public String export() {
        throw new UnsupportedOperationException();
    }

    /**
     * 关闭
     */
    @Override
    public void close() {}

    private static class AsnycCsvFileExporter implements Callable<Void> {
        private final Table table;
        private final boolean withBom;
        private final String savingFilePath;

        private AsnycCsvFileExporter(Table table, boolean withBom, 
                                     String savingFilePath) {
            this.table = table;
            this.withBom = withBom;
            this.savingFilePath = savingFilePath + ".csv";
        }

        @Override
        public Void call() throws Exception {
            table.end();
            try (CsvFileExporter csv = new CsvFileExporter(savingFilePath, withBom)) {
                csv.build(table);
            }
            return null;
        }
    }

}
