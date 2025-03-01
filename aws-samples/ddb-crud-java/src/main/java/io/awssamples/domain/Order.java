package io.awssamples.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
@DynamoDbBean
@Setter
@ToString
@EqualsAndHashCode
public class Order {

    @JsonIgnore
    public static final String TABLE_NAME = "orders";

    @JsonProperty("id")
    private String id;

    @JsonProperty("category")
    private String category;

    @JsonProperty("country")
    private String country;

    @JsonProperty("ck-country-state")
    private String ckCountryState;

    @JsonProperty("sku")
    private String sku;

    @JsonProperty("order-date")
    private String orderDate;

    @JsonProperty("query-slot-mod64")
    private int querySlotMod64;

    @JsonProperty("qty")
    private int qty;

    @JsonProperty("unit-price")
    private double pricePerUnit;

    @JsonProperty("state")
    private String state;

    @JsonProperty("payment-type")
    private String paymentType;

    @JsonProperty("comment")
    private String comment;

    public Order() {
    }

    @Builder
    public Order(String id,
                 String category,
                 String country,
                 String ckCountryState,
                 String sku,
                 String orderDate,
                 int querySlotMod64,
                 int qty,
                 double pricePerUnit,
                 String state,
                 String paymentType,
                 String comment) {
        this.id = id;
        this.category = category;
        this.country = country;
        this.ckCountryState = ckCountryState;
        this.sku = sku;
        this.orderDate = orderDate;
        this.querySlotMod64 = querySlotMod64;
        this.qty = qty;
        this.pricePerUnit = pricePerUnit;
        this.state = state;
        this.paymentType = paymentType;
        this.comment = comment;
    }

    @DynamoDbPartitionKey
    @DynamoDbAttribute("id")
    public String getId() {
        return id;
    }

    @DynamoDbAttribute("category")
    @DynamoDbSecondaryPartitionKey(indexNames = {"category-order-date-index", "category-query-slot-mod64-index"})
    public String getCategory() {
        return category;
    }

    @DynamoDbAttribute(value = "country")
    @DynamoDbSecondaryPartitionKey(indexNames = {"country-order-date-index"})
    public String getCountry() {
        return country;
    }

    @DynamoDbAttribute(value = "ck-country-state")
    @DynamoDbSecondaryPartitionKey(indexNames = {"ck-country-state-order-date-index"})
    public String getCkCountryState() {
        return ckCountryState;
    }

    @DynamoDbAttribute(value = "sku")
    @DynamoDbSecondaryPartitionKey(indexNames = {"sku-order-date-index"})
    public String getSku() {
        return sku;
    }

    @DynamoDbAttribute(value = "order-date")
    @DynamoDbSecondarySortKey(indexNames = {"sku-order-date-index", "country-order-date-index", "category-order-date-index", "ck-country-state-order-date-index"})
    public String getOrderDate() {
        return orderDate;
    }

    @DynamoDbAttribute(value = "query-slot-mod64")
    @DynamoDbSecondarySortKey(indexNames = {"category-query-slot-mod64-index"})
    public int getQuerySlotMod64() {
        return querySlotMod64;
    }

    @DynamoDbAttribute("qty")
    public int getQty() {
        return qty;
    }

    @DynamoDbAttribute("unit-price")
    public double getPricePerUnit() {
        return pricePerUnit;
    }

    @DynamoDbAttribute(value = "state")
    public String getState() {
        return state;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = {"payment-type-index"})
    @DynamoDbAttribute(value = "payment-type")
    public String getPaymentType() {
        return paymentType;
    }

    @DynamoDbAttribute(value = "comment")
    public String getComment() {
        return comment;
    }
}