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
// ===========================================================================================
// ON Macbook - i9 intel, 16BG Ram, 16 cores:
// -------------------------------------------------------------------------------------------
// Result "io.awssamples.JmhBenchmarkPaymentTypeIndexQueryRunner.asyncQuery":
//  37942.521 ±(99.9%) 3242.758 ms/op [Average]
//  (min, avg, max) = (34164.823, 37942.521, 41189.713), stdev = 2144.884
//  CI (99.9%): [34699.763, 41185.279] (assumes normal distribution)
//
// Result "io.awssamples.JmhBenchmarkPaymentTypeIndexQueryRunner.syncQuery":
//  40794.175 ±(99.9%) 4355.488 ms/op [Average]
//  (min, avg, max) = (36721.694, 40794.175, 46066.963), stdev = 2880.886
//  CI (99.9%): [36438.687, 45149.664] (assumes normal distribution)
//
// Benchmark                                           Mode  Cnt      Score      Error  Units
// JmhBenchmarkPaymentTypeIndexQueryRunner.asyncQuery  avgt   10  37942.521 ± 3242.758  ms/op
// JmhBenchmarkPaymentTypeIndexQueryRunner.syncQuery   avgt   10  40794.175 ± 4355.488  ms/op
// ===========================================================================================
