package io.awssamples;

import io.awssamples.domain.Order;
import io.awssamples.persistence.DynamoDbAsyncPaginatedOrderSearch;
import io.awssamples.persistence.DynamoDbPaginatedOrderSearchScan;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) {
        System.out.println("Demo scan!");

        try (var dynamoDbClient = AwsClientProvider.dynamoDbClient();
             var dynamoDbAsyncClient = AwsClientProvider.dynamoDbAsyncClient()) {

            var enhancedClient = AwsClientProvider.dynamoDbEnhancedClient(dynamoDbClient);
//            System.out.println("Start pagination scanning in sync mode");
//            scanSync(enhancedClient);
//            System.out.println("Complete pagination scanning in sync mode");

            var dynamoDbEnhancedAsyncClient = AwsClientProvider.dynamoDbEnhancedAsyncClient(dynamoDbAsyncClient);
            System.out.println("Start pagination scanning in async mode");
            scanAsync(dynamoDbEnhancedAsyncClient);
            System.out.println("Complete pagination scanning in async mode");

//            System.out.println("Start pagination scanning in parallel mode");
//            scanParallel(enhancedClient);
//            System.out.println("Complete pagination scanning in parallel mode");
        }
    }

    private static void scanSync(DynamoDbEnhancedClient enhancedClient) {
//        Total items count: 192308
//        Taken time : 00:08:14

        measureExecution(() -> {
            var paginatedOrderSearch = new DynamoDbPaginatedOrderSearchScan(enhancedClient);
            var pageIterable = paginatedOrderSearch.search("ProductNumber-1", 100);

            var total = pageIterable.stream()
                    .mapToInt(orderPage ->
                            // DO TRANSACTIONAL UPDATE
                            orderPage.items().size())
                    .sum();

            System.out.println("Total items count: " + total);
        });
    }

    private static void scanAsync(DynamoDbEnhancedAsyncClient asyncClient) {
//      Total items count: 192308
//      Taken time : 00:08:25

        measureExecution(() -> {
            var paginatedOrderSearch = new DynamoDbAsyncPaginatedOrderSearch(asyncClient);
            var orderPagePublisher = paginatedOrderSearch.search("ProductNumber-1", 100);

            orderPagePublisher.subscribe(new Subscriber<>() {
                private final CountDownLatch latch = new CountDownLatch(1);
                private Subscription subscription;
                private int total;

                @Override
                public void onSubscribe(Subscription sub) {
                    subscription = sub;
                    subscription.request(1L);
                    try {
                        latch.await(); // Called by main thread blocking it until latch is released.
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onNext(Page<Order> orderPage) {
                    // count items in the page, then ask the publisher for one more page.
                    total += orderPage.items().size();
                    subscription.request(1L);
                    // DO TRANSACTIONAL UPDATE
                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onComplete() {
                    System.out.println("Total items count: " + total);
                    latch.countDown(); // Call by subscription thread; latch releases.
                }
            });
        });
    }

    private static void scanParallel(DynamoDbEnhancedClient enhancedClient) {
//        Segment 0 count: 12059
//        Segment 1 count: 11925
//        Segment 2 count: 12108
//        Segment 3 count: 11982
//        Segment 4 count: 11944
//        Segment 5 count: 12188
//        Segment 6 count: 12118
//        Segment 7 count: 12170
//        Segment 8 count: 12048
//        Segment 9 count: 12031
//        Segment 10 count: 11983
//        Segment 11 count: 12047
//        Segment 12 count: 11804
//        Segment 13 count: 11831
//        Segment 14 count: 12075
//        Segment 15 count: 11995
//        Total count: 192308
//        Taken time : 00:00:40

        measureExecution(() -> {
            var table = enhancedClient.table(Order.TABLE_NAME, TableSchema.fromBean(Order.class));

            int totalSegments = Runtime.getRuntime().availableProcessors(); // Adjust based on table size and provisioned throughput

            var executor = Executors.newFixedThreadPool(totalSegments);
            var futures = new ArrayList<CompletableFuture<Integer>>();
            var segmentCounts = new ConcurrentHashMap<Integer, AtomicInteger>();

            for (var segment = 0; segment < totalSegments; segment++) {
                var currentSegment = segment;
                segmentCounts.put(currentSegment, new AtomicInteger(0));

                futures.add(CompletableFuture.supplyAsync(() -> {
                    var scanEnhancedRequest = ScanEnhancedRequest.builder()
                            .filterExpression(Expression.builder()
                                    .expression("#ProductNumber = :productNumber")
                                    .putExpressionName("#ProductNumber", Order.PRODUCT_NUMBER_FIELD_NAME)
                                    .putExpressionValue(":productNumber", AttributeValue.fromS("ProductNumber-1"))
                                    .build())
                            .segment(currentSegment)
                            .totalSegments(totalSegments)
                            .limit(100) // Adjust page size for pagination
                            .build();

                    for (var orderPage : table.scan(scanEnhancedRequest)) {
                        // DO TRANSACTIONAL UPDATE
                        segmentCounts.get(currentSegment).addAndGet(orderPage.items().size());
                    }

                    return segmentCounts.get(currentSegment).get();
                }, executor));
            }

            // Wait for all tasks to complete and sum counts
            var totalCount = futures.stream()
                    .map(CompletableFuture::join)
                    .mapToInt(Integer::intValue)
                    .sum();

            executor.shutdown(); // Shut down the executor after completion

            // Print segment counts
            segmentCounts.forEach((segment, count) ->
                    System.out.println("Segment " + segment + " count: " + count));

            System.out.println("Total count: " + totalCount);
        });
    }

    static void measureExecution(Runnable runnable) {
        var startTime = System.nanoTime();

        runnable.run();

        var durationInSeconds = (System.nanoTime() - startTime) / 1_000_000_000;

        var hours = durationInSeconds / 3600;
        var minutes = (durationInSeconds % 3600) / 60;
        var seconds = durationInSeconds % 60;

        System.out.printf("Taken time : %02d:%02d:%02d%n", hours, minutes, seconds);
    }
}