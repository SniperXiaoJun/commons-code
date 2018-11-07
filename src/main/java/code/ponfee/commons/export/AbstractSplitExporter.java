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
public abstract class AbstractSplitExporter extends AbstractExporter {

    private final int batchSize;
    protected final String savingFilePathPrefix;
    private final ExecutorService executor;

    public AbstractSplitExporter(int batchSize, String savingFilePathPrefix, 
                                 ExecutorService executor) {
        this.batchSize = batchSize;
        this.savingFilePathPrefix = savingFilePathPrefix;
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
                service.submit(splitExporter(st, split.incrementAndGet()));
            }
        });
        if (!subTable.get().isEmptyTbody()) {
            super.nonEmpty();
            service.submit(splitExporter(subTable.get(), split.incrementAndGet()));
        }

        MultithreadExecutor.joinDiscard(service, split.get(), AWAIT_TIME_MILLIS);
    }

    protected abstract AsnycSplitExporter splitExporter(Table subTable, int number);

    public @Override final Object export() {
        throw new UnsupportedOperationException();
    }

    public @Override final void close() {}

    public static abstract class AsnycSplitExporter implements Callable<Void> {
        protected final Table subTable;
        protected final String savingFilePath;

        public AsnycSplitExporter(Table subTable, String savingFilePathPrefix, 
                                  String suffix) {
            this.subTable = subTable;
            this.savingFilePath = savingFilePathPrefix + suffix;
        }

        @Override
        public final Void call() throws Exception {
            subTable.end();
            build();
            return null;
        }

        protected abstract void build();
    }

}
