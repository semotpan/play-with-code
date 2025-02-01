package io.awssamples.persistence;

import io.awssamples.domain.Order;
import io.awssamples.domain.Orders;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;
import software.amazon.awssdk.services.dynamodb.model.Update;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static software.amazon.awssdk.services.dynamodb.model.ReturnValuesOnConditionCheckFailure.ALL_OLD;

public class DynamoDbOrdersRepository implements Orders {

    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;

    private final DynamoDbTable<Order> orderTable;

    public DynamoDbOrdersRepository(DynamoDbClient dynamoDbClient, DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.dynamoDbClient = dynamoDbClient;
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
        this.orderTable = dynamoDbEnhancedClient.table(Order.TABLE_NAME, TableSchema.fromBean(Order.class));
    }

    @Override
    public PageIterable<Order> find(String productNumber, int pageSize) {
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 100;
        }

        ScanEnhancedRequest scanEnhancedRequest = ScanEnhancedRequest.builder()
                .filterExpression(Expression.builder()
                        .expression("#ProductNumber = :productNumber")
                        .putExpressionName("#ProductNumber", Order.PRODUCT_NUMBER_FIELD_NAME)
                        .putExpressionValue(":productNumber", AttributeValue.fromS(productNumber))
                        .build())
                .build();
        return orderTable.scan(scanEnhancedRequest);
    }

    @Override
    public boolean transactUpdate(List<Order> batch, UUID transactionToken) {
        // Prepare the transact write items
        List<TransactWriteItem> transactWriteItems = batch.stream()
                .map(order -> Update.builder()
                        .tableName(Order.TABLE_NAME)
                        .key(Map.of(
                                Order.ORDER_ID_FIELD_NAME, AttributeValue.fromS(order.getOrderId()),
                                Order.PRODUCT_NUMBER_FIELD_NAME, AttributeValue.fromS(order.getProductNumber())
                        ))
                        .expressionAttributeValues(Map.of(
                                ":orderStatus", AttributeValue.fromS(order.getOrderStatus())
                        ))
                        .updateExpression("SET %s = :orderStatus".formatted(Order.ORDER_STATUS_FIELD_NAME))
                        .returnValuesOnConditionCheckFailure(ALL_OLD)
                        .build())
                .map(updateItem -> TransactWriteItem.builder()
                        .update(updateItem)
                        .build())
                .collect(Collectors.toList());

        if (!transactWriteItems.isEmpty()) {
            try {
                // Execute the transaction
                TransactWriteItemsRequest transactWriteItemsRequest = TransactWriteItemsRequest.builder()
                        .transactItems(transactWriteItems)
                        .build();

                dynamoDbClient.transactWriteItems(transactWriteItemsRequest);
                return true; // Transaction successful
            } catch (Exception e) {
                // Handle transaction cancellation (e.g., due to a conflict)
                System.err.println("Transaction was canceled: " + e.getMessage());
            }
        }

        return false; // Return false if the transaction fails
    }
}
