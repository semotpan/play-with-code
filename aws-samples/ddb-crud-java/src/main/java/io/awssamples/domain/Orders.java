package io.awssamples.domain;

import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;

import java.util.List;
import java.util.UUID;

public interface Orders {

    PageIterable<Order> find(String productNumber, int pageSize);

    boolean transactUpdate(List<Order> batch, UUID transactionToken);

}
