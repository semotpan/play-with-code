package io.example.multithreading;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CmpletableFeatureSample {


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        var hello = submit("Hello");



        var empty = submit(null);


        var all = CompletableFuture.allOf(hello, empty);
//        all.join();

        assert "Hello".equals(hello.get());
        assert "Hello".equals(empty.get());
    }

    private static CompletableFuture<Optional<String>> submit(String message) {
        return CompletableFuture.supplyAsync(() -> Optional.ofNullable(message));
    }

    private CompletableFuture waitAndReturn(long millis, String value) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(millis);
                return value;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private CompletableFuture waitAndThrow(long millis) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(millis);
            } finally {
                throw new RuntimeException();
            }
        });
    }
}
