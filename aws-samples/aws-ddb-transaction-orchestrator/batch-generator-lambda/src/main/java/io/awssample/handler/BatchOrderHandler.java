package io.awssample.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awssample.DaggerBatchGeneratorComponent;
import io.awssample.domain.Batch;
import io.awssample.domain.Batches;
import io.awssample.domain.Order;
import io.awssample.domain.Orders;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

public class BatchOrderHandler implements RequestHandler<OrderUpdateRequest, List<String>> {

    // first set TTL property
    static {
        java.security.Security.setProperty("networkaddress.cache.ttl", "0");
    }

    @Inject
    ObjectMapper objectMapper;

    @Inject
    Batches batches;

    @Inject
    Orders orders;

    public BatchOrderHandler() {
        DaggerBatchGeneratorComponent.builder()
                .build()
                .inject(this);
    }

    @Override
    public List<String> handleRequest(OrderUpdateRequest orderUpdateRequest, Context context) {
        LambdaLogger logger = context.getLogger();

        logger.log("Starting fetch batches for customer number: %s... ".formatted(orderUpdateRequest.customerNumber()));
        var batchList = batches.find(orderUpdateRequest.customerNumber());
        logger.log("Scanned batches size: %s\n".formatted(batchList.size()));

        if (batchList.isEmpty()) {
            // fetch orders
            logger.log("No batches exists, start fetching orders for customer number %s\n".formatted(orderUpdateRequest.customerNumber()));
            var orderList = orders.find(orderUpdateRequest.customerNumber());
            logger.log("Scanned order size: %s\n".formatted(orderList.size()));

            logger.log("Stating partitioning order list by 1000 items... \n");
            var batchPartitioned = new BatchPartitioned<>(orderList, 1000);
            logger.log("Created %d partitions for provided order list \n".formatted(batchPartitioned.size()));

            if (batchPartitioned.size() > 100) {
                throw new IllegalArgumentException("Too many batches found for provided order list");
            }

            logger.log("Persisting batches ... \n");
            var createBatchList = batchPartitioned.partitions().stream()
                    .map(partition -> Batch.builder()
                            .batchId(UUID.randomUUID().toString())
                            .customerNumber(orderUpdateRequest.customerNumber())
                            .requestOrderStatus(orderUpdateRequest.status())
                            .orderSnapshots(transactionalOrderBatches(partition))
                            .build())
                    .toList();

            batches.add(createBatchList);

            logger.log("Batches created successfully \n");

            batchList = createBatchList;
        } else {
            // filter only batches with unprocessed items
            logger.log("Filtering batches with unprocessed items... \n");

            batchList = batchList.stream()
                    .filter(batch -> batch.getTransactionalOrderBatches().stream()
                            .anyMatch(item -> !item.getCommitted()))
                    .toList();
        }

        // build output
        return batchList.stream()
                .map(Batch::getBatchId)
                .toList();
    }

    @NotNull
    private List<Batch.TransactionalOrderBatch> transactionalOrderBatches(List<Order> orderList) {
        var transcationalOrderBatchPartitioned = new BatchPartitioned<>(orderList, 100);
        return transcationalOrderBatchPartitioned.partitions().stream()
                .map(partition -> partition.stream()
                        .map(Order::toOrderSnapshot)
                        .toList())
                .map(orderSnapshots -> new Batch.TransactionalOrderBatch(false, orderSnapshots))
                .toList();
    }
}
