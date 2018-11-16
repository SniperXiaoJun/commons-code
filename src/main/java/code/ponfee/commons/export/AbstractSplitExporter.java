package code.ponfee.commons.export;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Preconditions;

import code.ponfee.commons.concurrent.MultithreadExecutor;
import code.ponfee.commons.util.Holder;

/**
 * Export multiple file
 *
 * @author fupf
 */
public abstract class AbstractSplitExporter extends AbstractExporter<Void> {

    private final int batchSize;
    private final String savingFilePathPrefix;
    private final String fileSuffix;
    private final ExecutorService executor;

    public AbstractSplitExporter(int batchSize, String savingFilePathPrefix, 
                                 String fileSuffix, ExecutorService executor) {
        Preconditions.checkArgument(batchSize > 0);
        this.batchSize = batchSize;
        this.savingFilePathPrefix = savingFilePathPrefix;
        this.fileSuffix = fileSuffix;
        this.executor = executor;
    }

    public @Override final void build(Table table) {
        CompletionService<Void> service = new ExecutorCompletionService<>(executor);
        AtomicInteger count = new AtomicInteger(0);
        AtomicInteger split = new AtomicInteger(0);
        Holder<Table> subTable = Holder.of(table.copyOfWithoutTbody());
        rollingTbody(table, (data, i) -> {
            subTable.get().addRow(data);
            if (count.incrementAndGet() == batchSize) {
                super.nonEmpty();
                Table st = subTable.set(table.copyOfWithoutTbody());
                count.set(0); // reset count and sub table
                service.submit(splitExporter(st, buildFilePath(split.incrementAndGet())));
            }
        });
        if (!subTable.get().isEmptyTbody()) {
            super.nonEmpty();
            service.submit(splitExporter(subTable.get(), buildFilePath(split.incrementAndGet())));
        }

        MultithreadExecutor.joinDiscard(service, split.get(), AWAIT_TIME_MILLIS);
    }

    protected abstract AsnycSplitExporter splitExporter(Table subTable, String savingFilePath);

    public @Override final Void export() {
        throw new UnsupportedOperationException();
    }

    public @Override final void close() {}

    private String buildFilePath(int fileNo) {
        return savingFilePathPrefix + fileNo + fileSuffix;
    }

    public static abstract class AsnycSplitExporter implements Callable<Void> {
        private final Table subTable;
        protected final String savingFilePath;

        public AsnycSplitExporter(Table subTable, String savingFilePath) {
            this.subTable = subTable;
            this.savingFilePath = savingFilePath;
        }

        @Override
        public final Void call() throws Exception {
            subTable.end();
            try (AbstractExporter<?> exporter = createExporter()) {
                exporter.build(subTable);
                doOthers(exporter);
            }
            return null;
        }

        protected abstract AbstractExporter<?> createExporter();

        protected void doOthers(AbstractExporter<?> exporter) {}
    }

}
