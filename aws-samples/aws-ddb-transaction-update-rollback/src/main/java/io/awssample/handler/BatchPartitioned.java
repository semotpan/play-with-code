package io.awssample.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class BatchPartitioned<T> {

    private final int batchSize;
    private final List<List<T>> partitions;

    public BatchPartitioned(Collection<T> items) {
        requireNonNull(items, "items must not be null");

        this.batchSize = 100;

        if (items.size() < this.batchSize) {
            this.partitions = new ArrayList<>(1);
            this.partitions.add(new ArrayList<>(items));
            return;
        }

        // Calculate the number of partitions
        int numberOfPartitions = (int) Math.ceil((double) items.size() / batchSize);

        this.partitions = new ArrayList<>(numberOfPartitions);

        var itemList = new ArrayList<T>(items);
        int start = 0, end = batchSize;
        for (int part = 0; part < numberOfPartitions - 1; part++) {
            partitions.add(new ArrayList<>(itemList.subList(start, end)));
            start = end;
            end += batchSize;
        }

        if (start < items.size()) {
            partitions.add(new ArrayList<>(itemList.subList(start, items.size())));
        }
    }

    public List<List<T>> partitions() {
        return partitions;
    }

    public int size() {
        return partitions.size();
    }

    public int batchSize() {
        return batchSize;
    }

    public List<T> partition(int index) {
        if (index < 0 || index >= partitions.size()) {
            throw new IllegalArgumentException("Invalid partition index, allowed [0-%s]".formatted(partitions.size() - 1));
        }

        return partitions.get(index);
    }

    @Override
    public String toString() {
        return "BatchPartitioned{" +
                "batchSize=" + batchSize +
                ", partitionSize=" + partitions.size() +
                "}\n";
    }
}
