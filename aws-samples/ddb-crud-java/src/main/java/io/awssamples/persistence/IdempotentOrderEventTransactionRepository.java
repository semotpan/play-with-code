package io.awssamples.persistence;

import io.awssamples.domain.ConsumedEvent;
import io.awssamples.domain.IdempotentOrderEventTransaction;
import io.awssamples.domain.Order;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactUpdateItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.Map;

import static java.util.Objects.nonNull;
import static software.amazon.awssdk.services.dynamodb.model.BatchStatementErrorCodeEnum.CONDITIONAL_CHECK_FAILED;
import static software.amazon.awssdk.services.dynamodb.model.ReturnValuesOnConditionCheckFailure.ALL_OLD;

public final class IdempotentOrderEventTransactionRepository implements IdempotentOrderEventTransaction {

    private final DynamoDbClient dynamoDbClient;
    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<Order> orderTable;
    private final DynamoDbTable<ConsumedEvent> eventTable;

    public IdempotentOrderEventTransactionRepository(DynamoDbClient dynamoDbClient, DynamoDbEnhancedClient enhancedClient) {
        this.dynamoDbClient = dynamoDbClient;
        this.enhancedClient = enhancedClient;
        this.orderTable = enhancedClient.table(Order.TABLE_NAME, TableSchema.fromBean(Order.class));
        this.eventTable = enhancedClient.table(ConsumedEvent.TABLE_NAME, TableSchema.fromBean(ConsumedEvent.class));
    }

    @Override
    public boolean apply(Order order) {
        System.out.println("Running transaction...");
        var consumedEvent = ConsumedEvent.builder()
                .orderId(order.getId())
                .quantity(order.getQty())
                .state(order.getState())
                .build();

        // Step 1: Check if the event already exists (FIXME: is this useless??)
        if (isEventAlreadyProcessed(order)) {
            System.out.println("Skip processing event: " + consumedEvent + " exists");
            return true; // Skip processing as event exists
        }

        try {
            // Step 2: Attempt to update Order and insert Event in a transaction
            processOrderAndEvent(order, consumedEvent);
            System.out.println("Order quantity & query-slot-mod64 updated (summed) & Event created successfully");
            return true; // Success
        } catch (TransactionCanceledException e) {
            // Step 3: Handle failure and attempt to create Order and Event
            return handleTransactionFailure(order, consumedEvent, e);
        } catch (DynamoDbException e) {
            System.out.println("An exception occurred : " + e.getMessage());
            return false;
        }
    }

    private boolean isEventAlreadyProcessed(Order order) {
        var item = eventTable.getItem(
                GetItemEnhancedRequest.builder()
                        .key(Key.builder().partitionValue(order.getId()).build())
                        .consistentRead(true)
                        .build()
        );
        return nonNull(item);
    }

    private void processOrderAndEvent(Order order, ConsumedEvent consumedEvent) {
        var transactionRequest = TransactWriteItemsRequest.builder()
                .transactItems(
                        createOrderUpdateTransactionItem(order),
                        createEventInsertTransactionItem(consumedEvent)
                )
                .build();

        dynamoDbClient.transactWriteItems(transactionRequest);
    }

    private TransactWriteItem createOrderUpdateTransactionItem(Order order) {
        return TransactWriteItem.builder()
                .update(Update.builder()
                        .tableName(Order.TABLE_NAME)
                        .key(Map.of("id", AttributeValue.fromS(order.getId())))
                        .updateExpression("SET qty = if_not_exists(qty, :zero) + :newQty, #slot = if_not_exists(#slot, :zero) + :slot")
                        .conditionExpression("attribute_exists(id)")
                        .expressionAttributeValues(Map.of(
                                ":zero", AttributeValue.fromN("0"),
                                ":newQty", AttributeValue.fromN(Integer.valueOf(order.getQty()).toString()),
                                ":slot", AttributeValue.fromN(Integer.valueOf(order.getQuerySlotMod64()).toString())
                        ))
                        .expressionAttributeNames(Map.of(
                                "#slot", "query-slot-mod64"  // Mapping attribute name for reserved word
                        ))
                        .returnValuesOnConditionCheckFailure(ALL_OLD)
                        .build())
                .build();
    }

    private TransactWriteItem createEventInsertTransactionItem(ConsumedEvent consumedEvent) {
        return TransactWriteItem.builder()
                .put(Put.builder()
                        .tableName(ConsumedEvent.TABLE_NAME)
                        .item(Map.of(
                                "orderId", AttributeValue.fromS(consumedEvent.getOrderId()),
                                "occurredOn", AttributeValue.fromS(consumedEvent.getOccurredOn().toString()),
                                "quantity", AttributeValue.fromN(consumedEvent.getQuantity().toString()),
                                "state", AttributeValue.fromS(consumedEvent.getState())))
                        .conditionExpression("attribute_not_exists(orderId)")
                        .build())
                .build();
    }

    private boolean handleTransactionFailure(Order order, ConsumedEvent consumedEvent, TransactionCanceledException e) {
        if (!e.hasCancellationReasons()) {
            System.err.println("Failed to update order: " + order + ", no cancellation reasons stacktrace: " + e);
            return false; // Transaction failed, return false
        }

        if (!isIsConditionalCheckFailed(e)) {
            System.err.println("Failed to update order: " + order + ", no 'ConditionalCheckFailed' reason: " + e);
            return false; // Transaction failed, return false
        }

        System.err.println("Order does not exist, creating order and event... ");

        try {
            // Step 4: Attempt to create Order and Event
            createOrderAndEvent(order, consumedEvent);
            System.out.println("Order and Event created successfully");
            return true; // Success
        } catch (TransactionCanceledException ex) {
            if (e.hasCancellationReasons() && isIsConditionalCheckFailed(ex)) {
                System.err.println("A consumed event or order already exists, fail transaction, try again ..." + ex);
            } else {
                System.err.println("Transaction failed: " + ex);
            }

            return false; // Transaction failed, return false
        }
    }

    private boolean isIsConditionalCheckFailed(TransactionCanceledException e) {
        return e.cancellationReasons().stream()
                .map(CancellationReason::code)
                .anyMatch(code -> CONDITIONAL_CHECK_FAILED.toString().equalsIgnoreCase(code));
    }

    private void createOrderAndEvent(Order order, ConsumedEvent consumedEvent) {
        var transactionRequest = TransactWriteItemsEnhancedRequest.builder()
                .addUpdateItem(orderTable, TransactUpdateItemEnhancedRequest.builder(Order.class)
                        .item(order)
                        .conditionExpression(Expression.builder()
                                .expression("attribute_not_exists(id)")
                                .build())
                        .returnValuesOnConditionCheckFailure(ALL_OLD)
                        .build())
                .addUpdateItem(eventTable, TransactUpdateItemEnhancedRequest.builder(ConsumedEvent.class)
                        .item(consumedEvent)
                        .conditionExpression(Expression.builder()
                                .expression("attribute_not_exists(orderId)")
                                .build())
                        .returnValuesOnConditionCheckFailure(ALL_OLD)
                        .build())
                .build();

        enhancedClient.transactWriteItems(transactionRequest);
    }
}
