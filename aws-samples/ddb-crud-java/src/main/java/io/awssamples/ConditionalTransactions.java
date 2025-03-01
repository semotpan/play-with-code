package io.awssamples;

import io.awssamples.persistence.IdempotentOrderEventTransactionRepository;

public class ConditionalTransactions {

    public static void main(String[] args) {
        System.out.println("Running transactions...");

        var order = DynamoDbDataGenerator.generate();

        order.setId("9070476a-553a-41d0-b2d9-adeab7da8fd0");

        System.out.println("Created order: " + order);

        try (var dynamoDbClient = AwsClientProvider.dynamoDbClient()) {
            var enhancedClient = AwsClientProvider.dynamoDbEnhancedClient(dynamoDbClient);

            var idempotentOrderEventTransaction = new IdempotentOrderEventTransactionRepository(dynamoDbClient, enhancedClient);
            System.out.println("Transaction status: " + idempotentOrderEventTransaction.apply(order));
        }
    }
}
