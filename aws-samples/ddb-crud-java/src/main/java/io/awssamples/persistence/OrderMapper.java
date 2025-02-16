package io.awssamples.persistence;

import io.awssamples.domain.Order;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import java.util.Map;

final class OrderMapper {
    static Order fromDynamoDB(Map<String, AttributeValue> item) {
        if (item == null || item.isEmpty()) {
            return null;
        }

        return Order.builder()
                .id(getString(item, "id"))
                .category(getString(item, "category"))
                .country(getString(item, "country"))
                .ckCountryState(getString(item, "ck-country-state"))
                .sku(getString(item, "sku"))
                .orderDate(getString(item, "order-date"))
                .querySlotMod64(getInt(item, "query-slot-mod64"))
                .qty(getInt(item, "qty"))
                .pricePerUnit(getDouble(item, "unit-price"))
                .state(getString(item, "state"))
                .paymentType(getString(item, "payment-type"))
                .comment(getString(item, "comment"))
                .build();
    }

    private static String getString(Map<String, AttributeValue> item, String key) {
        return item.containsKey(key) && item.get(key).s() != null ? item.get(key).s() : null;
    }

    private static int getInt(Map<String, AttributeValue> item, String key) {
        return item.containsKey(key) && item.get(key).n() != null ? Integer.parseInt(item.get(key).n()) : 0;
    }

    private static double getDouble(Map<String, AttributeValue> item, String key) {
        return item.containsKey(key) && item.get(key).n() != null ? Double.parseDouble(item.get(key).n()) : 0.0;
    }
}
