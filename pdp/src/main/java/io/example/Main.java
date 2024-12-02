package io.example;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static io.example.csv.CSVParser.parallelCSVParser;

public class Main {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        EmployeeRepository employeeRepository = new EmployeeRepository();
        try (ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            CompletableFuture.supplyAsync(
                            () -> parallelCSVParser("customers-50000.csv", nameParser()).values())
                    .thenAccept(employees -> {
                        ItemPartitioner<Employee> itemPartitioner = new ItemPartitioner<>(employees);
                        for (int partition = 0; partition < itemPartitioner.partitions().size(); partition++) {
                            executorService.submit(new BatchTask<>(
                                    itemPartitioner.partition(partition),
                                    partition,
                                    employeeRepository::add,
                                    25
                            ));
                        }

                    })
                    .get();
        }
    }

    static Function<String[], Employee> nameParser() {
        return row -> new Employee(null, row[2], row[3]);
    }
}
