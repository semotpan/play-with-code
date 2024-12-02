package io.example.csv;

import com.opencsv.CSVReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

final class SimpleCSVParser<T> implements CSVParser<T> {

    private static final Logger logger = LogManager.getLogger(SimpleCSVParser.class);

    private final Collection<T> rows;

    public SimpleCSVParser(String fileName, Function<String[], T> rowMapper) {
        logger.info("Starting simple parsing CSV file '{}'... ", fileName);

        LongAdder counter = new LongAdder();
        long startTime = System.nanoTime();

        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(fileName));
        } catch (FileNotFoundException e) {
            logger.error("Failed to read file {}", fileName, e);
            throw new RuntimeException(e);
        }

        // skip header
        var iterator = reader.iterator();
        iterator.next();
        counter.increment();

        this.rows = new HashSet<>();
        while (iterator.hasNext()) {
            var line = iterator.next();
            rows.add(rowMapper.apply(line));
            counter.increment();
        }

        long endTime = System.nanoTime();

        logger.info("Completed simple parsing CSV file '{}' took '{}' seconds, total rows parsed '{}', unique rows '{}'",
                fileName, SECONDS.convert(endTime - startTime, NANOSECONDS), counter.longValue(), rows.size());
    }

    @Override
    public Collection<T> values() {
        return rows;
    }
}
