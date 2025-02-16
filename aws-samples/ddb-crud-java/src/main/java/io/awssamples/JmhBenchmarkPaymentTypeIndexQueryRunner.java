package io.awssamples;

import io.awssamples.domain.OrdersByPaymentTypeIndexQuery;
import io.awssamples.persistence.AsyncPaginationOrdersByPaymentTypeIndexQuery;
import io.awssamples.persistence.SyncPaginationOrdersByPaymentTypeIndexQuery;
import org.openjdk.jmh.annotations.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime) // Measures average execution time
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread) // A new instance per thread
@Warmup(iterations = 2) // 2 warmup iterations
@Measurement(iterations = 5) // 5 actual measurement iterations
@Fork(2)
public class JmhBenchmarkPaymentTypeIndexQueryRunner {

    DynamoDbClient dynamoDbClient;
    DynamoDbAsyncClient dynamoDbAsyncClient;
    OrdersByPaymentTypeIndexQuery asyncQuery;
    OrdersByPaymentTypeIndexQuery syncQuery;

    @Setup(Level.Trial)
    public void setup() {
        dynamoDbAsyncClient = AwsClientProvider.dynamoDbAsyncClient();
        asyncQuery = new AsyncPaginationOrdersByPaymentTypeIndexQuery(dynamoDbAsyncClient, "payment-type-index");

        dynamoDbClient = AwsClientProvider.dynamoDbClient();
        syncQuery = new SyncPaginationOrdersByPaymentTypeIndexQuery(dynamoDbClient, "payment-type-index");
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        dynamoDbAsyncClient.close();
        dynamoDbClient.close();
    }

    @Benchmark
    public void asyncQuery() {
        System.out.print("'Async items size: " + asyncQuery.search("CASH").size() + "' taken time: ");
    }

    @Benchmark
    public void syncQuery() {
        System.out.print("'Sync items size: " + syncQuery.search("CASH").size() + "' taken time: ");
    }
}
