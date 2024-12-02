package io.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public final class Task<T> implements Callable<Long> {

    private static final Logger logger = LogManager.getLogger(Task.class);

    private final Collection<T> items;
    private final Integer partition;
    private final Consumer<T> runnable;

    public Task(Collection<T> items, Integer partition, Consumer<T> runnable) {
        this.items = items;
        this.partition = partition;
        this.runnable = runnable;
    }

    @Override
    public Long call() throws Exception {
        logger.info("Starting task partition '{}' thread: '{}', items size: {}",
                partition, Thread.currentThread().getName(), items.size());
        var startTime = System.nanoTime();

        int logStop = 0;
        long total = 0L, success = 0L, failure = 0L;
        for (T item : items) {
            total++;
            logStop++;

            try {
                runnable.accept(item);
                success++;
            } catch (Exception ex) {
                logger.error("Failed to execute task partition: {}, thread: {} item: {}",
                        partition, Thread.currentThread().getName(), item, ex);
                failure++;
            }

            if (logStop == 1000) {
                logStop = 0;
                logger.info("Task partition '{}' thread: '{}', executed: {}, remaining {}",
                        partition, Thread.currentThread().getName(), total, items.size()-total);
            }
         }

        var endTime = System.nanoTime();

        logger.info("Completed  task partition '{}' thread: '{}', items size: {}, took '{}' seconds, successful: {}, failure: {}",
                partition, Thread.currentThread().getName(), items.size(), SECONDS.convert(endTime - startTime, NANOSECONDS),
                success, failure);

        return success;
    }
}
