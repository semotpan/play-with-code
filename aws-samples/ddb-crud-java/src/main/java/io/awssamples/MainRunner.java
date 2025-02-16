package io.awssamples;

import io.awssamples.persistence.AsyncPaginationOrdersByPaymentTypeIndexQuery;
import io.awssamples.persistence.ParallelPaginationOrdersByPaymentTypeScan;
import io.awssamples.persistence.SyncPaginationOrdersByPaymentTypeIndexQuery;
import io.awssamples.persistence.SyncPaginationOrdersByPaymentTypeScan;

public class MainRunner {

    public static void main(String[] args) {
        try (var dynamoDbClient = AwsClientProvider.dynamoDbClient();
             var dynamoDbAsyncClient = AwsClientProvider.dynamoDbAsyncClient()) {

            var dynamoDbEnhancedClient = AwsClientProvider.dynamoDbEnhancedClient(dynamoDbClient);

            var syncPaginationScan = new SyncPaginationOrdersByPaymentTypeScan(dynamoDbEnhancedClient);
            var parallelPaginationScan = new ParallelPaginationOrdersByPaymentTypeScan(dynamoDbEnhancedClient);

            var asyncPaginationQuery = new AsyncPaginationOrdersByPaymentTypeIndexQuery(dynamoDbAsyncClient, "payment-type-index");
            var syncPaginationQuery = new SyncPaginationOrdersByPaymentTypeIndexQuery(dynamoDbClient, "payment-type-index");

            Benchmarks.measureExecution(() -> {
                System.out.println("==============================================");
                System.out.println("QUERY SYNC for pagination orders");
                var orders = syncPaginationQuery.search("CASH");
                System.out.println("QUERY SYNC found " + orders.size() + " orders");
                System.out.println("==============================================");
            });

            Benchmarks.measureExecution(() -> {
                System.out.println("==============================================");
                System.out.println("QUERY ASYNC for pagination orders");
                var orders = asyncPaginationQuery.search("CASH");
                System.out.println("QUERY ASYNC found " + orders.size() + " orders");
                System.out.println("==============================================");
            });

            Benchmarks.measureExecution(() -> {
                System.out.println("==============================================");
                System.out.println("SCAN SYNC for pagination orders");
                var orders = syncPaginationScan.search("CASH");
                System.out.println("SCAN SYNC found " + orders.size() + " orders");
                System.out.println("==============================================");
            });

            Benchmarks.measureExecution(() -> {
                System.out.println("==============================================");
                System.out.println("SCAN PARALLEL for pagination orders");
                var orders = parallelPaginationScan.search("CASH");
                System.out.println("SCAN PARALLEL found " + orders.size() + " orders");
                System.out.println("==============================================");
            });
        }
    }
}
