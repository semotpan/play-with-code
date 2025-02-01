package io.awssamples.persistence;

import io.awssamples.domain.Order;
import io.awssamples.domain.PaginatedOrderSearch;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class DynamoDbPaginatedOrderSearchQuery implements PaginatedOrderSearch {

    private final DynamoDbTable<Order> orderTable;

    public DynamoDbPaginatedOrderSearchQuery(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.orderTable = dynamoDbEnhancedClient.table(Order.TABLE_NAME, TableSchema.fromBean(Order.class));
    }

    @Override
    public PageIterable<Order> search(String productNumber, int pageSize) {
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 100;
        }

        // FIXME: is not working yet
        var queryEnhancedRequest = QueryEnhancedRequest.builder()
                .filterExpression(Expression.builder()
                        .expression("#ProductNumber = :productNumber")
                        .putExpressionName("#ProductNumber", Order.PRODUCT_NUMBER_FIELD_NAME)
                        .putExpressionValue(":productNumber", AttributeValue.fromS(productNumber))
                        .build())
                .limit(pageSize)
                .queryConditional(QueryConditional.sortBeginsWith(Key.builder()
                        .partitionValue("id#1")
                        .sortValue("cart#")
                        .build()))
                .scanIndexForward(false)
                .build();
        return orderTable.query(queryEnhancedRequest);
    }
}
