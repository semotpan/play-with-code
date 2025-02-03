package io.awssamples;

import com.github.javafaker.Faker;
import io.awssamples.domain.Order;
import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DynamoDbDataGenerator {

    static final Faker faker = new Faker();

    public static void main(String[] args) {
//        createTable();

        Main.measureExecution(DynamoDbDataGenerator::generateDataInParallel);
//        Main.measureExecution(DynamoDbDataGenerator::generateData);
    }

    private static void generateData() {
        try (var dynamoDbClient = AwsClientProvider.dynamoDbClient()) {
            var enhancedClient = AwsClientProvider.dynamoDbEnhancedClient(dynamoDbClient);

            DynamoDbTable<Order> orderTable = enhancedClient.table(Order.TABLE_NAME, TableSchema.fromBean(Order.class));

            Map<String, Integer> map = Map.of(
                    "ProductNumber-1", 500_000,
                    "ProductNumber-2", 10_000
            );

            map.forEach((productNumber, count) -> {
                for (var i = 0; i < count; i++) {
                    var orderBuilder = WriteBatch.builder(Order.class).mappedTableResource(orderTable);

                    for (var batchCounter = 0; batchCounter < 25 && i < count; batchCounter++, i++) {
                        String status = switch (i % 5) {
                            case 0 -> "PLACED";
                            case 1 -> "PROCESSING";
                            case 2 -> "SHIPPED";
                            case 3 -> "DELIVERED";
                            default -> "CANCELLED";
                        };
                        var order = new Order(UUID.randomUUID().toString(),
                                productNumber, status, faker.commerce().productName(), (i % 1000) + 1);

                        orderBuilder.addPutItem(order);
                    }

                    enhancedClient.batchWriteItem(BatchWriteItemEnhancedRequest
                            .builder()
                            .writeBatches(orderBuilder.build())
                            .build());
                }
            });
            System.out.println("Data generation completed.");
        }
    }

    private static void generateDataInParallel() {

        System.out.println("Generating data in parallel.");
        try (var dynamoDbClient = AwsClientProvider.dynamoDbClient();
             ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            var enhancedClient = AwsClientProvider.dynamoDbEnhancedClient(dynamoDbClient);

            DynamoDbTable<Order> orderTable = enhancedClient.table(Order.TABLE_NAME, TableSchema.fromBean(Order.class));

            Map<String, Integer> map = Map.of(
                    "ProductNumber-1", 500_000,
                    "ProductNumber-2", 10_000
            );


            List<CompletableFuture<Void>> futures = new ArrayList<>();

            map.forEach((productNumber, count) -> {
                for (int i = 0; i < count; i += 25) {
                    int finalI = i;
                    futures.add(CompletableFuture.runAsync(() -> {
                        var orderBuilder = WriteBatch.builder(Order.class).mappedTableResource(orderTable);

                        for (int batchCounter = 0; batchCounter < 25 && (finalI + batchCounter) < count; batchCounter++) {
                            String status = switch (batchCounter % 5) {
                                case 0 -> "PLACED";
                                case 1 -> "PROCESSING";
                                case 2 -> "SHIPPED";
                                case 3 -> "DELIVERED";
                                default -> "CANCELLED";
                            };
                            var order = new Order(UUID.randomUUID().toString(),
                                    productNumber, status, faker.commerce().productName(), ((finalI + batchCounter) % 1000) + 1);
                            orderBuilder.addPutItem(order);
                        }

                        enhancedClient.batchWriteItem(BatchWriteItemEnhancedRequest.builder()
                                .writeBatches(orderBuilder.build())
                                .build());
                    }, executor));
                }
            });

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            executor.shutdown();
            System.out.println("Data generation completed.");
        }
    }

    private static void createTable() {
        try (var dynamoDbClient = AwsClientProvider.dynamoDbClient()) {
            CreateTableRequest createTableRequest = CreateTableRequest.builder()
                    .tableName(Order.TABLE_NAME)
                    .keySchema(
                            KeySchemaElement.builder()
                                    .attributeName(Order.ORDER_ID_FIELD_NAME)
                                    .keyType(KeyType.HASH) // Partition key
                                    .build(),
                            KeySchemaElement.builder()
                                    .attributeName(Order.PRODUCT_NUMBER_FIELD_NAME)
                                    .keyType(KeyType.RANGE) // Sort key
                                    .build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder()
                                    .attributeName(Order.ORDER_ID_FIELD_NAME)
                                    .attributeType(ScalarAttributeType.S)
                                    .build(),
                            AttributeDefinition.builder()
                                    .attributeName(Order.PRODUCT_NUMBER_FIELD_NAME)
                                    .attributeType(ScalarAttributeType.S)
                                    .build()
                    )
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .build();

            dynamoDbClient.createTable(createTableRequest);
            System.out.println("Table " + Order.TABLE_NAME + " created successfully.");
        }
    }

    private static void createTABLE() {
        try (var dynamoDbClient = AwsClientProvider.dynamoDbClient()) {
            var enhancedClient = AwsClientProvider.dynamoDbEnhancedClient(dynamoDbClient);
            var orderTable = enhancedClient.table(Order.TABLE_NAME, TableSchema.fromBean(Order.class));

            // Create the DynamoDB table using the 'orderTable' DynamoDbTable instance.
            orderTable.createTable();

            try (DynamoDbWaiter waiter = DynamoDbWaiter.builder().client(dynamoDbClient).build()) { // DynamoDbWaiter is Autocloseable
                ResponseOrException<DescribeTableResponse> response = waiter
                        .waitUntilTableExists(builder -> builder.tableName(Order.TABLE_NAME).build())
                        .matched();
                DescribeTableResponse tableDescription = response.response().orElseThrow(
                        () -> new RuntimeException("%s table was not created.".formatted(Order.TABLE_NAME)));
                // The actual error can be inspected in response.exception()
                System.out.printf("%s table was created.\n", Order.TABLE_NAME);
            }

        }
    }
}
