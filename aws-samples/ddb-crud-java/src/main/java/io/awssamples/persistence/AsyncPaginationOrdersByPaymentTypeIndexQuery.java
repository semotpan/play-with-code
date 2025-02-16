package io.awssamples.persistence;

import io.awssamples.domain.Order;
import io.awssamples.domain.OrdersByPaymentTypeIndexQuery;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class AsyncPaginationOrdersByPaymentTypeIndexQuery implements OrdersByPaymentTypeIndexQuery {

    private final DynamoDbAsyncClient dynamoDbAsyncClient;
    private final String paymentTypeIndexName;

    public AsyncPaginationOrdersByPaymentTypeIndexQuery(DynamoDbAsyncClient dynamoDbAsyncClient,
                                                        String paymentTypeIndexName) {
        this.dynamoDbAsyncClient = Objects.requireNonNull(dynamoDbAsyncClient, "dynamoDbAsyncClient cannot be null");
        this.paymentTypeIndexName = Objects.requireNonNull(paymentTypeIndexName, "paymentTypeIndexName cannot be null");
    }

    @Override
    public List<Order> search(String paymentType) {
        var queryRequest = QueryRequest.builder()
                .indexName(paymentTypeIndexName)
                .tableName(Order.TABLE_NAME)
                .keyConditionExpression("#payment_type = :payment_type")
                .limit(10_000) // APPLY big page size, AWS should adjust up to 1 MB page size
                .expressionAttributeValues(Map.of(
                        ":payment_type", AttributeValue.fromS(paymentType)
                ))
                .expressionAttributeNames(Map.of(
                        "#payment_type", "payment-type"
                ))
                .build();

        var queryPublisher = dynamoDbAsyncClient.queryPaginator(queryRequest);
        return Flowable.fromPublisher(queryPublisher)
                .flatMapIterable(queryResponse -> {
                    // TODO apply some logic
                    return queryResponse.items().stream()
                            .map(OrderMapper::fromDynamoDB)
                            .toList();
                })
                .toList()
                .blockingGet();
    }
}
