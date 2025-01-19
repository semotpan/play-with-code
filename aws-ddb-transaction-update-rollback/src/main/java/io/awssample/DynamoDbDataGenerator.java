package io.awssample;

import io.awssample.domain.Order;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbRequest;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbResponse;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class DynamoDbDataGenerator {

    private static final String TABLE_NAME = "order";

    private static final List<String> STATUSES = Arrays.asList("PLACED", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED");
    private static final List<String> PRODUCTS = Arrays.asList("Product1", "Product2", "Product3", "Product4", "Product5");

    public static void main(String[] args) {

        // Credentials that can be replaced with real AWS values. (To be handled properly and not hardcoded.)
        // These can be skipped altogether for LocalStack, but we generally want to avoid discrepancies with production code.
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

            IntStream.range(0, 1000).forEach(i -> {
                String productNumber = "Product-" + (i / 100);
                String status = switch (i % 5) {
                    case 0 -> "PLACED";
                    case 1 -> "PROCESSING";
                    case 2 -> "SHIPPED";
                    case 3 -> "DELIVERED";
                    default -> "CANCELLED";
                };

                Order order = new Order(UUID.randomUUID().toString(), productNumber, status, "Product Name", (i % 10) + 1);

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
                                .attributeName("id")
                                .keyType(KeyType.HASH) // Partition key
                                .build(),
                        KeySchemaElement.builder()
                                .attributeName("productNumber")
                                .keyType(KeyType.RANGE) // Sort key
                                .build()
                )
                .attributeDefinitions(
                        AttributeDefinition.builder()
                                .attributeName("id")
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName("productNumber")
                                .attributeType(ScalarAttributeType.S)
                                .build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .build();

        dynamoDbClient.createTable(createTableRequest);
        System.out.println("Table " + TABLE_NAME + " created successfully.");
    }
}
