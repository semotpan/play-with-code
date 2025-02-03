package io.awssample.persistence;

import io.awssample.domain.Batches;
import io.awssample.domain.Order;
import io.awssample.domain.Orders;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;

class DynamoDBOrderRepository implements Orders {

    private static final String TABLE_NAME = "order";

    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private final DynamoDbTable<Order> orderTable;

    public DynamoDBOrderRepository(DynamoDbClient dynamoDbClient, DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.dynamoDbClient = dynamoDbClient;
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
        this.orderTable = dynamoDbEnhancedClient.table(TABLE_NAME, TableSchema.fromBean(Order.class));
    }

    @Override
    public List<Order> find(String customerNumber) {
        return orderTable.scan(ScanEnhancedRequest.builder()
                        .filterExpression(Expression.builder()
                                .expression("#customerNumber = :customerNumber")
                                .putExpressionName("#customerNumber", "CustomerNumber")
                                .putExpressionValue(":customerNumber", AttributeValue.fromS(customerNumber))
                                .build())
                        .build())
                .items()
                .stream()
                .toList();
    }
}
