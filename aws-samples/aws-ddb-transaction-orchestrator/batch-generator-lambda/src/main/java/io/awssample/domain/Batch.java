package io.awssample.domain;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@DynamoDbBean
@NoArgsConstructor
@EqualsAndHashCode
public final class Batch {

    private String batchId;
    private String customerNumber;
    private Order.OrderStatus requestOrderStatus;
//    private String messageQueueId;
    private List<TransactionalOrderBatch> transactionalOrderBatches;

    @Builder
    public Batch(String batchId, String customerNumber, Order.OrderStatus requestOrderStatus, List<TransactionalOrderBatch> orderSnapshots) {
        this.batchId = batchId;
        this.customerNumber = customerNumber;
        this.requestOrderStatus = requestOrderStatus;
//        this.messageQueueId = messageQueueId;
        this.transactionalOrderBatches = new ArrayList<>(orderSnapshots);
    }

    @DynamoDbPartitionKey
    @DynamoDbAttribute("BatchId")
    public String getBatchId() {
        return batchId;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("CustomerNumber")
    public String getCustomerNumber() {
        return customerNumber;
    }

    @DynamoDbAttribute("RequestOrderStatus")
    public Order.OrderStatus getRequestOrderStatus() {
        return requestOrderStatus;
    }

//    @DynamoDbAttribute("MessageQueueId")
//    public String getMessageQueueId() {
//        return messageQueueId;
//    }

    @DynamoDbAttribute("TransactionalOrderBatches")
    public List<TransactionalOrderBatch> getTransactionalOrderBatches() {
        return transactionalOrderBatches;
    }

    @Override
    public String toString() {
        return "Batch{" +
                "batchId=" + batchId +
                ", customerNumber='" + customerNumber + '\'' +
                ", requestOrderStatus=" + requestOrderStatus +
//                ", messageQueueId='" + messageQueueId + '\'' +
                ", transactionalOrderBatches.size=" + transactionalOrderBatches.size() +
                '}';
    }

    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static class TransactionalOrderBatch {

        private Boolean committed;
        private List<OrderSnapshot> orderSnapshots;

        @DynamoDbAttribute("Committed")
        public Boolean getCommitted() {
            return committed;
        }

        @DynamoDbAttribute("OrderSnapshots")
        public List<OrderSnapshot> getOrderSnapshots() {
            return orderSnapshots;
        }
    }
}
