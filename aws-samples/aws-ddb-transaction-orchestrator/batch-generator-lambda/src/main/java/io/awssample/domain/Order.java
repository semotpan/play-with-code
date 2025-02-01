package io.awssample.domain;


import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.util.UUID;

@DynamoDbBean
@ToString
@NoArgsConstructor
@Setter
@EqualsAndHashCode
public class Order {

    private String orderId;
    private String customerNumber;
    private OrderStatus status;

    public Order(String orderId, String customerNumber, OrderStatus status) {
        this.orderId = orderId;
        this.customerNumber = customerNumber;
        this.status = status;
    }

    @DynamoDbPartitionKey
    @DynamoDbAttribute("OrderId")
    public String getOrderId() {
        return orderId;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("CustomerNumber")
    public String getCustomerNumber() {
        return customerNumber;
    }

    @DynamoDbAttribute("Status")
    public OrderStatus getStatus() {
        return status;
    }

    public OrderSnapshot toOrderSnapshot() {
        return new OrderSnapshot(orderId, status);
    }

    public enum OrderStatus {
        NEW,
        COMPLETED,
        FAILED;
    }
}
