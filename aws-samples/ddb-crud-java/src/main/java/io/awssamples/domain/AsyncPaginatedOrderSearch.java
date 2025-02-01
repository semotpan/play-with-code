package io.awssamples.domain;

import software.amazon.awssdk.enhanced.dynamodb.model.PagePublisher;

public interface AsyncPaginatedOrderSearch {

    PagePublisher<Order> search(String productNumber, int pageSize);

}
