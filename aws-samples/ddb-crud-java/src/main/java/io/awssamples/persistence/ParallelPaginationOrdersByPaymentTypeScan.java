package io.awssamples.persistence;

import io.awssamples.domain.Order;
import io.awssamples.domain.OrdersByPaymentTypeScan;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ParallelPaginationOrdersByPaymentTypeScan implements OrdersByPaymentTypeScan {

    private final int totalSegments;
    private final DynamoDbTable<Order> orderTable;
    private final ExecutorService executor;

    public ParallelPaginationOrdersByPaymentTypeScan(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.totalSegments = Runtime.getRuntime().availableProcessors();
        this.executor = Executors.newFixedThreadPool(totalSegments);
        this.orderTable = dynamoDbEnhancedClient.table(Order.TABLE_NAME, TableSchema.fromBean(Order.class));
    }

    @Override
    public List<Order> search(String paymentType) {
        var tasks = new ArrayList<CompletableFuture<List<Order>>>();
        var segmentCounts = new ConcurrentHashMap<Integer, CopyOnWriteArrayList<Order>>();

        for (var segment = 0; segment < totalSegments; segment++) {
            var currentSegment = segment;
            segmentCounts.put(currentSegment, new CopyOnWriteArrayList<>());

            tasks.add(CompletableFuture.supplyAsync(() -> {
                var scanEnhancedRequest = ScanEnhancedRequest.builder()
                        .filterExpression(Expression.builder()
                                .expression("#payment_type = :payment_type")
                                .putExpressionName("#payment_type", "payment-type")
                                .putExpressionValue(":payment_type", AttributeValue.fromS(paymentType))
                                .build())
                        .limit(10_000)
                        .build();

                for (var orderPage : orderTable.scan(scanEnhancedRequest)) {
                    // DO TRANSACTIONAL UPDATE
                    segmentCounts.get(currentSegment).addAll(orderPage.items());
                }

                return segmentCounts.get(currentSegment);
            }, executor));
        }

        // Wait for all tasks to complete and sum counts
        var orders = tasks.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .toList();

        executor.shutdown(); // Shut down the executor after completion

        return orders;
    }
}
