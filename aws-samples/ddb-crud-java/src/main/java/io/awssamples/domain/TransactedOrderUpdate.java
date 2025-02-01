package io.awssamples.domain;

import java.util.List;
import java.util.UUID;

public interface TransactedOrderUpdate {

    boolean update(List<Order> batch, UUID transactionToken);

}
