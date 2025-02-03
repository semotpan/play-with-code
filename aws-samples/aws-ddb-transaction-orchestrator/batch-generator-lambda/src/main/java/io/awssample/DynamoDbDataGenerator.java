package io.awssample;

import io.awssample.domain.Order;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.net.URI;
import java.util.UUID;
import java.util.stream.IntStream;

import static software.amazon.awssdk.services.dynamodb.model.StreamViewType.NEW_AND_OLD_IMAGES;

public class DynamoDbDataGenerator {

    private static final String TABLE_NAME = "order";

    public static void main(String[] args) {

        // Credentials that can be replaced with real AWS values. (To be handled properly and not hardcoded.)
        final String ACCESS_KEY = "key";
        final String SECRET_KEY = "secret";

        // Creating the AWS Credentials provider, using the above access and secret keys.
        AwsCredentialsProvider credentials = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY));

        // Selected region.
        Region region = Region.EU_WEST_1;

        try (DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
                .region(region)
                .credentialsProvider(credentials)
                .endpointOverride(URI.create("https://localhost.localstack.cloud:4566"))
                .build()) {

            DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                    .dynamoDbClient(dynamoDbClient)
                    .build();

            createTable(dynamoDbClient);

            DynamoDbTable<Order> orderTable = enhancedClient.table(TABLE_NAME, TableSchema.fromBean(Order.class));

            IntStream.range(0, 10_000).forEach(i -> {
                String customerNumber = "CustomerNumber-" + (i / 1000);
                Order.OrderStatus status = switch (i % 4) {
                    case 0 -> Order.OrderStatus.NEW;
                    case 1 -> Order.OrderStatus.COMPLETED;
                    default-> Order.OrderStatus.FAILED;
                };

                Order order = new Order(UUID.randomUUID().toString(), customerNumber, status);

                orderTable.putItem(order);
            });

            System.out.println("Data generation completed.");
        }
    }

    private static void createTable(DynamoDbClient dynamoDbClient) {
        CreateTableRequest createTableRequest = CreateTableRequest.builder()
                .tableName(TABLE_NAME)
                .keySchema(
                        KeySchemaElement.builder()
                                .attributeName("OrderId")
                                .keyType(KeyType.HASH) // Partition key
                                .build(),
                        KeySchemaElement.builder()
                                .attributeName("CustomerNumber")
                                .keyType(KeyType.RANGE) // Sort key
                                .build()
                )
                .attributeDefinitions(
                        AttributeDefinition.builder()
                                .attributeName("OrderId")
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName("CustomerNumber")
                                .attributeType(ScalarAttributeType.S)
                                .build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .streamSpecification(StreamSpecification.builder()
                        .streamEnabled(true)
                        .streamViewType(NEW_AND_OLD_IMAGES)
                        .build())
                .build();

        dynamoDbClient.createTable(createTableRequest);
        System.out.println("Table " + TABLE_NAME + " created successfully.");
    }
}
