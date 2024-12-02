package io.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public final class BatchTask<T> implements Callable<Long> {

    private static final Logger logger = LogManager.getLogger(BatchTask.class);

    private final Collection<T> items;
    private final Integer partition;
    private final Consumer<Collection<T>> runnable;
    private final int batchSize;

    public BatchTask(Collection<T> items, Integer partition, Consumer<Collection<T>> runnable, int batchSize) {
        this.items = items;
        this.partition = partition;
        this.runnable = runnable;
        this.batchSize = batchSize;
    }

    @Override
    public Long call() throws Exception {
        logger.info("Starting batch task partition '{}' thread: '{}', items size: {}",
                partition, Thread.currentThread().getName(), items.size());
        var startTime = System.nanoTime();

        long total = 0L, success = 0L, failure = 0L;
        int logStop = 0;
        Collection<T> batchItems = new ArrayList<>(batchSize);

        for (T item : items) {
            total++;
            logStop++;

            batchItems.add(item);
            if (batchItems.size() < batchSize && total + 1 < items.size()) {
                continue;
            }

            try {
                runnable.accept(new ArrayList<>(batchItems));
                success += batchItems.size();
            } catch (Exception ex) {
                logger.error("Failed to execute batch task partition: {}, thread: {} item: {}",
                        partition, Thread.currentThread().getName(), batchItems, ex);
                failure += batchItems.size();
            }

            batchItems.clear();
            if (logStop == 1000) {
                logStop = 0;
                logger.info("Batch task partition '{}' thread: '{}', executed: {}, remaining {}",
                        partition, Thread.currentThread().getName(), total, items.size() - total);
            }
        }

        var endTime = System.nanoTime();

        logger.info("Completed batch task partition '{}' thread: '{}', items size: {}, took '{}' seconds, successful: {}, failure: {}",
                partition, Thread.currentThread().getName(), items.size(), SECONDS.convert(endTime - startTime, NANOSECONDS),
                success, failure);

        return success;
    }
}
