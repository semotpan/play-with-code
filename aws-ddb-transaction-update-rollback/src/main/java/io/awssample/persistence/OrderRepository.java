package io.awssample.persistence;

import io.awssample.domain.Order;

import java.util.List;

public interface OrderRepository {

    List<Order> find(String productNumber);

    boolean updateStatus(List<String> ids, String newStatus);

}
