package io.awssample.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awssample.domain.Order;
import io.awssample.persistence.OrderRepository;

import javax.inject.Inject;
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

    @Override
    public String handleRequest(SQSEvent event, Context context) {

        // TODO: parse input lockId, usageHash


        context.getLogger().log("Starting fetch orders ");
        var orders = orderRepository.find("Product-1");
        context.getLogger().log("Orders size: " + orders.size());

        // TODO: fetch recordingAggregateIds and current status by usageHash

        // TODO: ? save current state somewhere in case of failure, to be able to restore

        // TODO: partition in batches found ids, by 100?

        // TODO: submit async each batch to DDB in a separate transaction

        // TODO: check if any failures of a batch, cancel all processes

        // TODO: in case of success: remove the persisted lock by ID

        // TODO: in case of failure take old state and submit async to rollback to the initial state

        // TODO: ? remove temp save data

        // TODO: end the process

        return "";
    }
}
