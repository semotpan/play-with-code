package io.awssamples.persistence;

import io.awssamples.domain.Order;
import io.awssamples.domain.OrdersByPaymentTypeScan;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;

public class SyncPaginationOrdersByPaymentTypeScan implements OrdersByPaymentTypeScan {

    private final DynamoDbTable<Order> orderTable;

    public SyncPaginationOrdersByPaymentTypeScan(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.orderTable = dynamoDbEnhancedClient.table(Order.TABLE_NAME, TableSchema.fromBean(Order.class));
    }

    @Override
    public List<Order> search(String paymentType) {
        var scanEnhancedRequest = ScanEnhancedRequest.builder()
                .filterExpression(Expression.builder()
                        .expression("#payment_type = :payment_type")
                        .putExpressionName("#payment_type", "payment-type")
                        .putExpressionValue(":payment_type", AttributeValue.fromS(paymentType))
                        .build())
                .limit(10_000)
                .build();

        return orderTable.scan(scanEnhancedRequest)
                .items()
                .stream()
                .toList();
    }
}
