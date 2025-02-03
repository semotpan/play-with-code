package io.awssample.domain;

import java.util.List;

public interface Batches {

    List<Batch> find(String customerNumber);

    /**
     * Max allowed batch size is 100, at least 1
     */
    void add(List<Batch> batches);

}
