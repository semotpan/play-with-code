package io.awssamples.domain;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class Order {

    public static final String TABLE_NAME = "orders";

    public static final String ORDER_ID_FIELD_NAME = "OrderId";
    public static final String PRODUCT_NUMBER_FIELD_NAME = "ProductNumber";
    public static final String ORDER_STATUS_FIELD_NAME = "OrderStatus";
    public static final String PRODUCT_NAME_FIELD_NAME = "ProductName";
    public static final String QUANTITY_FIELD_NAME = "Quantity";

    private String orderId;
    private String productNumber;
    private String orderStatus;
    private String productName;
    private int quantity;

    public Order() {
    }

    public Order(String orderId,
                 String productNumber,
                 String orderStatus,
                 String productName,
                 int quantity) {
        this.orderId = orderId;
        this.productNumber = productNumber;
        this.orderStatus = orderStatus;
        this.productName = productName;
        this.quantity = quantity;
    }

    @DynamoDbAttribute("OrderId")
    @DynamoDbPartitionKey
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("ProductNumber")
    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
    }

    @DynamoDbAttribute("OrderStatus")
    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    @DynamoDbAttribute("ProductName")
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    @DynamoDbAttribute("Quantity")
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
