package io.awssample.handler;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.CreateEventSourceMappingRequest;
import software.amazon.awssdk.services.lambda.model.CreateFunctionRequest;
import software.amazon.awssdk.services.lambda.model.FunctionCode;
import software.amazon.awssdk.services.lambda.model.Runtime;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testcontainers.containers.localstack.LocalStackContainer.*;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.*;

//@Tag("component")
@Testcontainers
class SQSProductHandlerE2ETest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQSProductHandlerE2ETest.class);

    private static final String TABLE_NAME = "product";
    private static final String QUEUE_NAME = "test-queue";
    private static final String LAMBDA_NAME = "ProductLambda";

    @Container
    public static final LocalStackContainer localstack =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:4.0.3"))
                    .withEnv("PRODUCT_TABLE_NAME", TABLE_NAME) // FIXME: this is skipped
                    .withEnv("DEBUG", "true")
                    .withLogConsumer(new Slf4jLogConsumer(LOGGER))
                    .withServices(SQS, LAMBDA, DYNAMODB, EnabledService.named("events"));

    @Test
    void e2e() throws Exception {
        // Create clients
        SqsClient sqsClient = SqsClient.builder()
                .endpointOverride(localstack.getEndpointOverride(SQS))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())
                        )
                )
                .region(Region.of(localstack.getRegion()))
                .build();

        DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
                .endpointOverride(localstack.getEndpointOverride(DYNAMODB))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())
                        )
                )
                .region(Region.of(localstack.getRegion()))
                .build();

        LambdaClient lambdaClient = LambdaClient.builder()
                .endpointOverride(localstack.getEndpointOverride(LAMBDA))
                .credentialsProvider(
                        StaticCredentialsProvider.create(AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey()))
                )
                .region(Region.of(localstack.getRegion()))
                .build();

        // Setup resources
        CreateQueueResponse queueResponse = sqsClient.createQueue(CreateQueueRequest.builder()
                .queueName(QUEUE_NAME)
                .build());

        createDynamoDBTable(dynamoDbClient);
        deployLambda(lambdaClient);

        // Trigger Lambda via SQS
        var payload = """
                {
                    "name": "Test Product",
                    "price": 20.5
                }
                """;

        sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(queueResponse.queueUrl())
                .messageBody(payload)
                .build());

        // Validate data in DynamoDB
//        Thread.sleep(15000); // Allow time for the Lambda to process
//        GetItemResponse response = dynamoDbClient.getItem(GetItemRequest.builder()
//                .tableName(TABLE_NAME)
//                .key(Map.of("id", AttributeValue.builder().s("123").build()))
//                .build());

        Thread.sleep(15000);

//        lambdaClient.waiter()
//                .waitUntilFunctionActiveV2(GetFunctionRequest.builder()
//                        .functionName(LAMBDA_NAME)
//                        .build());

        ScanResponse scanResponse = dynamoDbClient.scan(ScanRequest.builder()
                .tableName(TABLE_NAME)
                .limit(1)
                .build());

        assertEquals("Test Product", scanResponse.items().get(0).get("name").s());
    }

    private void createDynamoDBTable(DynamoDbClient dynamoDbClient) {
        dynamoDbClient.createTable(CreateTableRequest.builder()
                .tableName(TABLE_NAME)
                .keySchema(KeySchemaElement.builder()
                        .attributeName("id")
                        .keyType(KeyType.HASH)
                        .build())
                .attributeDefinitions(
                        AttributeDefinition.builder()
                                .attributeName("id")
                                .attributeType(ScalarAttributeType.S)
                                .build())
                .provisionedThroughput(ProvisionedThroughput.builder()
                        .readCapacityUnits(1L)
                        .writeCapacityUnits(1L)
                        .build())
                .build());
    }

    private void deployLambda(LambdaClient lambdaClient) throws Exception {
        // Package Lambda code into a ZIP file (assumes you have a compiled JAR)
        byte[] lambdaCode = Files.readAllBytes(Paths.get("target/product-lambda-0.0.1-SNAPSHOT.jar"));

        // Create Lambda function
        lambdaClient.createFunction(CreateFunctionRequest.builder()
                .functionName(LAMBDA_NAME)
                .runtime(Runtime.JAVA17)
                .role("arn:aws:iam::000000000000:role/lambda-role") // Mock role for LocalStack
                .handler("io.awssample.handler.SQSProductHandler::handleRequest") // Your handler class
                .code(FunctionCode.builder().zipFile(SdkBytes.fromByteArray(lambdaCode)).build())
                .build());

        // Create an Event Source Mapping to link the SQS queue to the Lambda function
        lambdaClient.createEventSourceMapping(CreateEventSourceMappingRequest.builder()
                .eventSourceArn("arn:aws:sqs:us-east-1:000000000000:%s".formatted(QUEUE_NAME))
                .functionName(LAMBDA_NAME)
                .batchSize(1)
                .enabled(true)
                .build());
    }
}
