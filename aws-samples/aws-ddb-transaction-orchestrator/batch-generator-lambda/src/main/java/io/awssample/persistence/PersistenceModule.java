package io.awssample.persistence;

import dagger.Module;
import dagger.Provides;
import io.awssample.domain.Batches;
import io.awssample.domain.Orders;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import javax.inject.Singleton;

@Module
public class PersistenceModule {

    @Provides
    @Singleton
    public static DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .region(Region.of(System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable())))
                .httpClient(UrlConnectionHttpClient.builder().build())
                .build();
    }

    @Provides
    @Singleton
    public static DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }

    @Provides
    @Singleton
    public static Batches batches(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        return new DynamoDBBatchRepository(dynamoDbEnhancedClient);
    }

    @Provides
    @Singleton
    public static Orders orders(DynamoDbClient dynamoDbClient, DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        return new DynamoDBOrderRepository(dynamoDbClient, dynamoDbEnhancedClient);
    }
}
