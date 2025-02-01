package io.awssamples;

import io.awssamples.domain.Order;
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

            DynamoDbTable<Order> orderTable = enhancedClient.table("order", TableSchema.fromBean(Order.class));

            IntStream.range(0, 10_000).forEach(i -> {
                String productNumber = "Product-" + (i / 1000);
                String status = switch (i % 5) {
                    case 0 -> "PLACED";
                    case 1 -> "PROCESSING";
                    case 2 -> "SHIPPED";
                    case 3 -> "DELIVERED";
                    default -> "CANCELLED";
                };

                Order order = new Order(UUID.randomUUID().toString(), productNumber, status, "Product Name", (i % 100) + 1);

                orderTable.putItem(order);
            });

            Order order1001 = new Order(UUID.randomUUID().toString(), "Product-1", "DELIVERED", "Product Name", 1);
            orderTable.putItem(order1001);

            System.out.println("Data generation completed.");
        }
    }

    private static void createTable(DynamoDbClient dynamoDbClient) {
        CreateTableRequest createTableRequest = CreateTableRequest.builder()
                .tableName(Order.TABLE_NAME)
                .keySchema(
                        KeySchemaElement.builder()
                                .attributeName("orderId")
                                .keyType(KeyType.HASH) // Partition key
                                .build(),
                        KeySchemaElement.builder()
                                .attributeName("productNumber")
                                .keyType(KeyType.RANGE) // Sort key
                                .build()
                )
                .attributeDefinitions(
                        AttributeDefinition.builder()
                                .attributeName("orderId")
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName("productNumber")
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
        System.out.println("Table " + Order.TABLE_NAME + " created successfully.");
    }
}
