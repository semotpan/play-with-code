package io.awssample;

import dagger.Module;
import dagger.Provides;
import io.awssample.persistence.DynamoDbOrderRepository;
import io.awssample.persistence.OrderRepository;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.SdkSystemSetting;
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
    public static OrderRepository orderRepository(DynamoDbClient dynamoDbClient) {
        return new DynamoDbOrderRepository(dynamoDbClient);
    }
}
