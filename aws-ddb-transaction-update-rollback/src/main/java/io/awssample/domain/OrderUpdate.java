package io.awssample.domain;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class OrderUpdate {
    private String orderId;
    private String productNumber;
    private String orderStatus;

    public OrderUpdate() {
    }

    public OrderUpdate(String orderId, String productNumber, String orderStatus) {
        this.orderId = orderId;
        this.productNumber = productNumber;
        this.orderStatus = orderStatus;
    }

    @DynamoDbPartitionKey
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @DynamoDbSortKey
    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}
