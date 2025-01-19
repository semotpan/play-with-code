package io.awssample.persistence;
import io.awssample.domain.Order;

import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.*;
import java.util.stream.Collectors;

public class DynamoDbOrderRepository implements OrderRepository {

    private static final String TABLE_NAME = "order";

    private final DynamoDbClient dynamoDbClient;

    public DynamoDbOrderRepository(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public List<Order> find(String productNumber) {
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .filterExpression("productNumber = :productNumber") // Filter by productNumber
                .expressionAttributeValues(Map.of(
                        ":productNumber", AttributeValue.builder().s(productNumber).build()
                ))
                .build();

        ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

        return scanResponse.items().stream().map(item -> new Order(
                item.get("id").s(),
                item.get("productNumber").s(),
                item.get("status").s(),
                item.get("product").s(),
                Integer.parseInt(item.get("quantity").n())
        )).collect(Collectors.toList());
    }

    @Override
    public boolean updateStatus(List<String> ids, String newStatus) {
        // Prepare the transact write items
        List<TransactWriteItem> transactWriteItems = new ArrayList<>();

        ids.forEach(id -> {
            Map<String, AttributeValue> key = Map.of(
                    "id", AttributeValue.builder().s(id).build()
            );

            Map<String, AttributeValue> expressionAttributeValues = Map.of(
                    ":status", AttributeValue.builder().s(newStatus).build()
            );

            // Create the update item for the transaction
            Update updateItem = Update.builder()
                    .updateExpression("SET status = :status")
                    .expressionAttributeValues(expressionAttributeValues)
                    .key(key)
                    .build();

            // Add the update item to the transaction
            transactWriteItems.add(TransactWriteItem.builder()
                    .update(updateItem)
                    .build());
        });

        if (!transactWriteItems.isEmpty()) {
            try {
                // Execute the transaction
                TransactWriteItemsRequest transactWriteItemsRequest = TransactWriteItemsRequest.builder()
                        .transactItems(transactWriteItems)
                        .build();

                dynamoDbClient.transactWriteItems(transactWriteItemsRequest);
                return true; // Transaction successful
            } catch (TransactionCanceledException e) {
                // Handle transaction cancellation (e.g., due to a conflict)
                System.err.println("Transaction was canceled: " + e.getMessage());
            } catch (DynamoDbException e) {
                // Handle DynamoDB-specific exceptions
                System.err.println("DynamoDB exception occurred: " + e.getMessage());
            }
        }

        return false; // Return false if the transaction fails
        }
}


