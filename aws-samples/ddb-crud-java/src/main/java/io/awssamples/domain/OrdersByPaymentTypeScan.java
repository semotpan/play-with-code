package io.awssamples.domain;

import java.util.List;

public interface OrdersByPaymentTypeScan {

    List<Order> search(String paymentType);

}
