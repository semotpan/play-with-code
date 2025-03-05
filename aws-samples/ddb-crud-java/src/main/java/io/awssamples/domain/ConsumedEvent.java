package io.awssamples.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.Instant;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.Objects.isNull;

@JsonInclude(NON_NULL)
@DynamoDbBean
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class ConsumedEvent {

    @JsonIgnore
    public static final String TABLE_NAME = "consumedEvents";

    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("occurredOn")
    private Instant occurredOn;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("query-slot-mod64")
    private Integer querySlotMod64;

    @Builder
    public ConsumedEvent(String orderId, Instant occurredOn, Integer quantity, Integer querySlotMod64) {
        this.orderId = orderId;
        this.occurredOn = isNull(occurredOn) ? Instant.now() : occurredOn;
        this.quantity = quantity;
        this.querySlotMod64 = querySlotMod64;
    }

    @DynamoDbPartitionKey
    @DynamoDbAttribute("orderId")
    public String getOrderId() {
        return orderId;
    }

    //    @DynamoDbSortKey
    @DynamoDbAttribute("occurredOn")
    public Instant getOccurredOn() {
        return occurredOn;
    }

    @DynamoDbAttribute("quantity")
    public Integer getQuantity() {
        return quantity;
    }

    @DynamoDbAttribute("query-slot-mod64")
    public Integer getQuerySlotMod64() {
        return querySlotMod64;
    }
}
