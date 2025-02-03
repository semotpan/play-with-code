package io.awssample.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awssample.DaggerOrderSyncComponent;
import io.awssample.domain.Order;
import io.awssample.domain.OrderUpdate;
import io.awssample.persistence.OrderRepository;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class SqsLockHandler implements RequestHandler<SQSEvent, String> {

    // first set TTL property
    static {
        java.security.Security.setProperty("networkaddress.cache.ttl", "0");
    }

    @Inject
    ObjectMapper objectMapper;

    @Inject
    OrderRepository orderRepository;

    public SqsLockHandler() {
        DaggerOrderSyncComponent.builder()
                .build()
                .inject(this);
    }

    @Override
    public String handleRequest(SQSEvent event, Context context) {
        // TODO: parse input lockId, usageHash
        LambdaLogger logger = context.getLogger();

        logger.log("SQSEvent size: %s \n".formatted(event.getRecords().size()));

        var commands = new ArrayList<ProductNumberLockedCommand>(event.getRecords().size());
        for (var message : event.getRecords()) {
            try {
                commands.add(objectMapper.readValue(message.getBody(), ProductNumberLockedCommand.class));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        logger.log("Commands : %s \n".formatted(commands.toString()));

        for (ProductNumberLockedCommand command : commands) {

            // TODO: fetch recordingAggregateIds and current status by usageHash
            logger.log("Starting fetch orders ... ");
            var orders = orderRepository.find(command.productNumber());
            logger.log("Scanned orders size: %s\n".formatted(orders.size()));

            // TODO: (CHECK point) ? save current state somewhere in case of failure, to be able to restore
            // !!!!! ?????

            // TODO: partition in batches found ids, by 100? (50 updates, 50 inserts)
            BatchPartitioned<Order> batchPartitioned = new BatchPartitioned<>(orders);

            // TODO: submit async each batch to DDB in a separate transaction
            // ???

            // TODO: submit sync each batch
            var orderedTransactedBatches = new ArrayList<List<Order>>();
            var failure = false;
            var partition = 0;
            for (; partition < batchPartitioned.size(); partition++) {

                List<Order> batch = batchPartitioned.partition(partition);

                List<OrderUpdate> orderUpdates = batch.stream()
                        .map(order -> order.orderUpdate("UPDATED"))
                        .toList();

                // FIXME: attempt 3 times to submit the batch
                var batchSucceeded = false;
                for (int attemts = 0; attemts < 3; attemts++) {
//                   FAIL LAST UPDATE AND TRIGGER ROLLBACK
//                    if (partition == batchPartitioned.size() - 1) {
//                        batchSucceeded = false;
//                        break;
//                    }

                    if (orderRepository.update(orderUpdates)) {
                        batchSucceeded = true;
                        break;
                    }
                }

                if (!batchSucceeded) {
                    failure = true;
                    break;
                }

                // Add successful batches
                orderedTransactedBatches.add(batch);
            }

            if (failure) {
                logger.log("There are failed to update orders\n");

                // FIXME: resubmit entire aggregates update?

                // ROLLBACK mechanism
                // THINK extracting this in a separate LAMBDA??
                for (var batch : orderedTransactedBatches) {

                    List<OrderUpdate> orderRollbacks = batch.stream()
                            .map(Order::orderRollback)
                            .toList();

                    // FIXME: attempt 3 times to submit the batch
                    for (int attemts = 0; attemts < 3; attemts++) {
                        if (orderRepository.update(orderRollbacks)) {
                            break;
                        }
                    }
                }
                logger.log("Items rollback successfully\n");
            } else {
                orderedTransactedBatches.clear();
                logger.log("Items updated successfully\n");
            }

            // TODO: check if any failures of a batch, cancel all processes

            // TODO: in case of success: remove the persisted lock by ID

            // TODO: in case of failure take old state and submit async to rollback to the initial state

            // TODO: ? remove temp save data

            // TODO: end the process
        }

        return "SUCCESS";
    }
}
