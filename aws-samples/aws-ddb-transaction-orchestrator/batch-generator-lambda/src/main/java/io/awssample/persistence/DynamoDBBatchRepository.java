package io.awssample.persistence;

import io.awssample.domain.Batch;
import io.awssample.domain.Batches;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactPutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.List;

import static software.amazon.awssdk.services.dynamodb.model.ReturnValuesOnConditionCheckFailure.ALL_OLD;

class DynamoDBBatchRepository implements Batches {

    private static final String TABLE_NAME = "order-transaction-batches";
    private static final String BATCH_PARTITION_KEY = "BatchId";

    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private final DynamoDbTable<Batch> batchTable;

    public DynamoDBBatchRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
        this.batchTable = dynamoDbEnhancedClient.table(TABLE_NAME, TableSchema.fromBean(Batch.class));
    }

    @Override
    public List<Batch> find(String customerNumber) {
        return batchTable.scan(ScanEnhancedRequest.builder()
                        .filterExpression(Expression.builder()
                                .expression("#customerNumber = :customerNumber")
                                .putExpressionName("#customerNumber", "CustomerNumber")
                                .putExpressionValue(":customerNumber", AttributeValue.fromS(customerNumber))
                                .build())
                        .build())
                .items()
                .stream()
                .toList();
    }

    @Override
    public void add(List<Batch> batches) {
        requireValidBatchList(batches);

        TransactWriteItemsEnhancedRequest.Builder itemsWriteBuilder = TransactWriteItemsEnhancedRequest.builder();

        batches.forEach(batch -> itemsWriteBuilder.addPutItem(batchTable, TransactPutItemEnhancedRequest.builder(Batch.class)
                .item(batch)
                .conditionExpression(Expression.builder()
                        .expression("attribute_not_exists (%s)".formatted(BATCH_PARTITION_KEY))
                        .build())
                .returnValuesOnConditionCheckFailure(ALL_OLD)
                .build()));

        dynamoDbEnhancedClient.transactWriteItems(itemsWriteBuilder.build());
    }

    private void requireValidBatchList(List<Batch> batches) {
        if (CollectionUtils.isNullOrEmpty(batches)) {
            throw new IllegalArgumentException("batches cannot be empty");
        }

        if (batches.size() > 100) {
            throw new IllegalArgumentException("batches size must be less than 100");
        }
    }
}
