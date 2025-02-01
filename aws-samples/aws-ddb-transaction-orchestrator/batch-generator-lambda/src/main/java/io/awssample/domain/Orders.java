package io.awssample.domain;

import java.util.List;

public interface Orders {

    List<Order> find(String customerNumber);

}
