package io.example.csv;

import java.util.Collection;
import java.util.function.Function;

public interface CSVParser<T> {

    Collection<T> values();

    static <T> CSVParser<T> simpleCSVParser(String fileName, Function<String[], T> rowMapper) {
        return new SimpleCSVParser<>(fileName, rowMapper);
    }

    static <T> CSVParser<T> parallelCSVParser(String fileName, Function<String[], T> rowMapper) {
        return new ParallelCSVParser<>(fileName, rowMapper);
    }
}
