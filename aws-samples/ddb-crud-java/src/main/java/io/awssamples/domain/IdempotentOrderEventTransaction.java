package io.awssamples.domain;

public interface IdempotentOrderEventTransaction {

    boolean apply(Order order);

}
