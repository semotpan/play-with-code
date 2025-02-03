package io.awssample.persistence;

import io.awssample.domain.Order;
import io.awssample.domain.OrderUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;
import software.amazon.awssdk.services.dynamodb.model.Update;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static software.amazon.awssdk.services.dynamodb.model.ReturnValuesOnConditionCheckFailure.ALL_OLD;

public class DynamoDbOrderRepository implements OrderRepository {

    static Logger logger = LoggerFactory.getLogger(DynamoDbOrderRepository.class);

    private static final String TABLE_NAME = "order";

    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private final DynamoDbTable<Order> orderTable;

    public DynamoDbOrderRepository(DynamoDbClient dynamoDbClient, DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.dynamoDbClient = dynamoDbClient;
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
        this.orderTable = dynamoDbEnhancedClient.table(TABLE_NAME, TableSchema.fromBean(Order.class));
    }

    @Override
    public List<Order> find(String productNumber) {
        return orderTable.scan(ScanEnhancedRequest.builder()
                        .filterExpression(Expression.builder()
                                .expression("#productNumber = :productNumber")
                                .putExpressionName("#productNumber", "productNumber")
                                .putExpressionValue(":productNumber", AttributeValue.fromS(productNumber))
                                .build())
                        .build())
                .items()
                .stream()
                .toList();
// --------------------------------------------- WITH DynamoClassic Client ------------------------
//        ScanRequest scanRequest = ScanRequest.builder()
//                .tableName(TABLE_NAME)
//                .filterExpression("productNumber = :productNumber") // Filter by productNumber
//                .expressionAttributeValues(Map.of(
//                        ":productNumber", AttributeValue.builder().s(productNumber).build()
//                ))
//                .build();
//
//        ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);
//
//        return scanResponse.items().stream().map(item -> new Order(
//                item.get("orderId").s(),
//                item.get("productNumber").s(),
//                item.get("orderStatus").s(),
//                item.get("product").s(),
//                Integer.parseInt(item.get("quantity").n())
//        )).collect(Collectors.toList());
// ------------------------------------------------------------------------------------------------
    }

    @Override
    public boolean update(List<OrderUpdate> orders) {
        // Prepare the transact write items
        List<TransactWriteItem> transactWriteItems = orders.stream()
                .map(order -> Update.builder()
                        .tableName(TABLE_NAME)
                        .key(Map.of(
                                "orderId", AttributeValue.fromS(order.getOrderId()),
                                "productNumber", AttributeValue.fromS(order.getProductNumber())
                        ))
                        .expressionAttributeValues(Map.of(
                                ":orderStatus", AttributeValue.fromS(order.getOrderStatus())
                        ))
                        .updateExpression("SET orderStatus = :orderStatus")
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
