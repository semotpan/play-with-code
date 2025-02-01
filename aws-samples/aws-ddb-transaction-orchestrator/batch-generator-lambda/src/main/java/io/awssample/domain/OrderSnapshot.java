package io.awssample.domain;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.util.UUID;

@DynamoDbBean
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class OrderSnapshot {

    private String orderId;
    private Order.OrderStatus oldStatus;

    @DynamoDbAttribute("OrderId")
    public String getOrderId() {
        return orderId;
    }

    @DynamoDbAttribute("OldStatus")
    public Order.OrderStatus getOldStatus() {
        return oldStatus;
    }
}
