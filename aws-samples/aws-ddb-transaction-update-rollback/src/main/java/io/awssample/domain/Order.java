package io.awssample.domain;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class Order {
    private String orderId;
    private String productNumber;
    private String orderStatus;
    private String product;
    private int quantity;

    public Order() {
    }

    public Order(String orderId, String productNumber, String orderStatus) {
        this.orderId = orderId;
        this.productNumber = productNumber;
        this.orderStatus = orderStatus;
    }

    public Order(String orderId, String productNumber, String orderStatus, String product, int quantity) {
        this.orderId = orderId;
        this.productNumber = productNumber;
        this.orderStatus = orderStatus;
        this.product = product;
        this.quantity = quantity;
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

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public OrderUpdate orderUpdate(String newStatus) {
        return new OrderUpdate(orderId, productNumber, newStatus);
    }

    public OrderUpdate orderRollback() {
        return new OrderUpdate(orderId, productNumber, orderStatus);
    }
}
