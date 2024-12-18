package io.awssample.handler;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;

import static io.awssample.handler.AWSTestSetup.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.testcontainers.containers.localstack.LocalStackContainer.EnabledService;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.*;

@Tag("component")
@Testcontainers
class SQSProductHandlerE2ECloudformationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQSProductHandlerE2ETest.class);

    @Container
    static LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:4.0.3"))
            .withCopyFileToContainer(MountableFile.forHostPath("src/test/resources/test-template-localstack.yaml"), "/app/template-localstack-test.yaml")
            .withCopyFileToContainer(MountableFile.forHostPath("target/product-lambda-0.0.1-SNAPSHOT.jar"), "/app/product-lambda-0.0.1-SNAPSHOT.jar")
            .withEnv(Map.of(
                    "DEBUG", "true",
                    "PRODUCT_TABLE_NAME", TEST_PRODUCT_TABLE_NAME
            ))
            .withLogConsumer(new Slf4jLogConsumer(LOGGER))
            .withServices(LAMBDA, DYNAMODB, SQS, S3, IAM, CLOUDFORMATION, EnabledService.named("events"));

    @BeforeAll
    static void setup() throws IOException, InterruptedException {
        // configure aws cli
        execInContainer("aws", "configure", "set", "aws_access_key_id", AWS_TEST_ACCESS_KEY_ID);
        execInContainer("aws", "configure", "set", "aws_secret_access_key", AWS_TEST_ACCESS_SECRET_KEY);
        execInContainer("aws", "configure", "set", "region", AWS_TEST_REGION);

        // create bucket
        execInContainer("aws", "--endpoint-url=http://localhost:4566", "s3", "mb", "s3://artifact-storage-bucket");

        // upload artifact to the s3 bucket
        execInContainer("aws", "--endpoint-url=http://localhost:4566", "s3", "cp", "/app/product-lambda-0.0.1-SNAPSHOT.jar", "s3://artifact-storage-bucket/product-lambda-0.0.1-SNAPSHOT.jar");

        // deploy cloudformation test template
        execInContainer("aws", "--endpoint-url=http://localhost:4566", "cloudformation", "deploy", "--stack-name", "cloudformation-localstack-test", "--template-file", "/app/template-localstack-test.yaml");
    }

    @Test
    void e2e() throws InterruptedException {
        var payload = """
                {
                    "name": "Test Product",
                    "price": 20.5
                }
                """;

        try (SqsClient sqsClient = buildSQSClient(); DynamoDbClient dynamoDbClient = buildDDBClient()) {
            // Retrieve the Queue URL
            GetQueueUrlResponse response = sqsClient.getQueueUrl(GetQueueUrlRequest.builder()
                    .queueName("product-sqs-queue")
                    .build());

            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(response.queueUrl())
                    .messageBody(payload)
                    .build());

            var productTableResult = new ArrayList<Map<String, AttributeValue>>();
            Awaitility.await()
                    .atMost(Duration.ofSeconds(30))
                    .with()
                    .pollInterval(Duration.ofSeconds(1))
                    .untilAsserted(() -> {
                        ScanResponse scanResponse = dynamoDbClient.scan(ScanRequest.builder()
                                .tableName("product")
                                .limit(1)
                                .build());
                        productTableResult.addAll(scanResponse.items());
                        assertThat(productTableResult).hasSize(1);
                    });

            assertThat(productTableResult)
                    .hasSize(1)
                    .extracting(
                            item -> item.get("name").s(),
                            item -> new BigDecimal(item.get("price").n())
                    )
                    .containsExactly(tuple("Test Product", BigDecimal.valueOf(20.5D)));
        }
    }

    private static void execInContainer(String... cmd) throws IOException, InterruptedException {
        org.testcontainers.containers.Container.ExecResult result = localstack.execInContainer(cmd);
        assertThat(result.getExitCode())
                .overridingErrorMessage("\n%s\n%s".formatted(result.getStdout(), result.getStderr()))
                .isZero();
    }

    private static DynamoDbClient buildDDBClient() {
        return DynamoDbClient.builder()
                .endpointOverride(localstack.getEndpointOverride(DYNAMODB))
                .credentialsProvider(credentialsProvider())
                .region(Region.of(AWS_TEST_REGION))
                .build();
    }

    private static SqsClient buildSQSClient() {
        return SqsClient.builder()
                .endpointOverride(localstack.getEndpointOverride(SQS)) // Use LocalStack's SQS endpoint
                .credentialsProvider(credentialsProvider()
                )
                .region(Region.of(AWS_TEST_REGION))
                .build();
    }

    private static StaticCredentialsProvider credentialsProvider() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())
        );
    }
}
