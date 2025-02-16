package io.awssamples.domain;

import java.util.List;

public interface OrdersByPaymentTypeIndexQuery {

    List<Order> search(String paymentType);

}
