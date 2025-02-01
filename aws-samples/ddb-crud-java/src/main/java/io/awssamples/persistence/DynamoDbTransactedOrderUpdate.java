package io.awssamples.persistence;

import io.awssamples.domain.Order;
import io.awssamples.domain.TransactedOrderUpdate;
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

public class DynamoDbTransactedOrderUpdate implements TransactedOrderUpdate {

    private final DynamoDbClient dynamoDbClient;

    public DynamoDbTransactedOrderUpdate(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public boolean update(List<Order> batch, UUID transactionToken) {
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
