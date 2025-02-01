package io.awssamples.domain;

import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;

public interface PaginatedOrderSearch {

    PageIterable<Order> search(String productNumber, int pageSize);

}
