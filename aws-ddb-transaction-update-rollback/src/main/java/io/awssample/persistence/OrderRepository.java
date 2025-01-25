package io.awssample.persistence;

import io.awssample.domain.Order;
import io.awssample.domain.OrderUpdate;

import java.util.List;

public interface OrderRepository {

    List<Order> find(String productNumber);

    boolean update(List<OrderUpdate> orders);

}
