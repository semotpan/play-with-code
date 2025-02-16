package io.awssamples;

import com.github.javafaker.Faker;
import io.awssamples.domain.Order;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class DynamoDbDataGenerator {

    private static final Faker faker = new Faker();
    private static final Random random = new Random();
    private static final String[] categories = {"ELECTRONICS", "FASHION", "HOME", "TOYS", "SPORTS", "BOOKS", "GROCERIES", "AUTOMOTIVE", "MUSIC", "GARDEN"};
    private static final String[] states = {"CA", "TX", "NY", "FL", "MA", "WA", "IL", "OH", "GA", "NC"};
    private static final String[] paymentTypes = {"DEBIT", "CASH"};

    public static void main(String[] args) {
        Benchmarks.measureExecution(() -> {
            try (var dynamoDbClient = AwsClientProvider.dynamoDbClient();
                 var executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {

                var enhancedClient = AwsClientProvider.dynamoDbEnhancedClient(dynamoDbClient);

                var orderTable = enhancedClient.table(Order.TABLE_NAME, TableSchema.fromBean(Order.class));

                var tasks = new ArrayList<CompletableFuture<Void>>();

                System.out.println("Generating data in parallel.");
                for (int i = 0; i < 20_000; i++) {

                    tasks.add(CompletableFuture.runAsync(() -> {
                        var orderBuilder = WriteBatch.builder(Order.class).mappedTableResource(orderTable);

                        // generate 25 items batch
                        IntStream.range(0, 25)
                                .mapToObj(val -> generate())
                                .forEach(orderBuilder::addPutItem);

                        enhancedClient.batchWriteItem(BatchWriteItemEnhancedRequest.builder()
                                .writeBatches(orderBuilder.build())
                                .build());

                    }, executor));
                }

                CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();
                executor.shutdown();
                System.out.println("Data generation completed.");
            }
        });
    }

    private static Order generate() {
        return Order.builder()
                .category(categories[random.nextInt(categories.length)])
                .ckCountryState("USA#" + states[random.nextInt(states.length)])
                .comment(faker.lorem().sentence())
                .country("USA")
                .id(UUID.randomUUID().toString())
                .orderDate(LocalDate.of(2024, random.nextInt(12) + 1, random.nextInt(28) + 1).toString())
                .paymentType(paymentTypes[random.nextInt(paymentTypes.length)])
                .qty(random.nextInt(100) + 1)
                .querySlotMod64(random.nextInt(64))
                .sku("A-" + (random.nextInt(999) + 1))
                .state(states[random.nextInt(states.length)])
                .pricePerUnit(BigDecimal.valueOf(random.nextDouble() * 100).doubleValue())
                .build();
    }
}
