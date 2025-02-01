package io.awssample.handler;

import io.awssample.domain.Order;

import java.util.UUID;

public record OrderUpdateRequest(String customerNumber, Order.OrderStatus status) {
}
