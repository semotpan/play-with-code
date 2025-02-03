package io.awssamples.persistence;

import io.awssamples.domain.Order;
import io.awssamples.domain.PaginatedOrderSearch;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class DynamoDbPaginatedOrderSearchScan implements PaginatedOrderSearch {

    private final DynamoDbTable<Order> orderTable;

    public DynamoDbPaginatedOrderSearchScan(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.orderTable = dynamoDbEnhancedClient.table(Order.TABLE_NAME, TableSchema.fromBean(Order.class));
    }

    @Override
    public PageIterable<Order> search(String productNumber, int pageSize) {
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
