package io.awssamples.persistence;

import io.awssamples.domain.AsyncPaginatedOrderSearch;
import io.awssamples.domain.Order;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.PagePublisher;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class DynamoDbAsyncPaginatedOrderSearch implements AsyncPaginatedOrderSearch {

    private final DynamoDbAsyncTable<Order> orderTable;

    public DynamoDbAsyncPaginatedOrderSearch(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient) {
        this.orderTable = dynamoDbEnhancedAsyncClient.table(Order.TABLE_NAME, TableSchema.fromBean(Order.class));
    }

    @Override
    public PagePublisher<Order> search(String productNumber, int pageSize) {
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 100;
        }

        ScanEnhancedRequest scanEnhancedRequest = ScanEnhancedRequest.builder()
                .filterExpression(Expression.builder()
                        .expression("#ProductNumber = :productNumber")
                        .putExpressionName("#ProductNumber", Order.PRODUCT_NUMBER_FIELD_NAME)
                        .putExpressionValue(":productNumber", AttributeValue.fromS(productNumber))
                        .build())
                .limit(pageSize)
                .build();


        return orderTable.scan(scanEnhancedRequest);
    }
}
