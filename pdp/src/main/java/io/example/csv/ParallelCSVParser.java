package io.example.csv;

import com.opencsv.CSVReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.CharArrayReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.nio.file.StandardOpenOption.READ;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toSet;

final class ParallelCSVParser<T> implements CSVParser<T> {

    private static final Logger logger = LogManager.getLogger(ParallelCSVParser.class);

    private final Collection<T> rows;

    public ParallelCSVParser(String fileName, Function<String[], T> rowMapper) {
        logger.info("Starting parallel parsing CSV file '{}'... ", fileName);

        LongAdder counter = new LongAdder();

        var startTime = System.nanoTime();

        try {

            var ps = new PartitionSpliterator(Path.of(fileName));
            this.rows = IntStream.range(0, ps.numberOfPartitions())
                    .parallel()
                    .mapToObj(part -> new PartitionReader<>(ps.partition(part), rowMapper, counter))
                    .flatMap(reader -> reader.rows().stream())
                    .collect(toSet());

        } catch (IOException e) {
            logger.error("Failed to read file {}", fileName, e);
            throw new RuntimeException(e);
        }

        var endTime = System.nanoTime();

        logger.info("Completed parallel parsing CSV file '{}' took '{}' seconds, total rows parsed '{}', unique rows '{}'",
                fileName, SECONDS.convert(endTime - startTime, NANOSECONDS), counter.longValue(), rows.size());
    }

    @Override
    public Collection<T> values() {
        return rows;
    }

    private static final class PartitionReader<T> {

        private final Set<T> rows;

        private PartitionReader(Partition partition, Function<String[], T> rowMapper, LongAdder counter) {
            CharArrayReader arrayReader = null;
            try {
                arrayReader = convertToCharArrayReader(partition);
            } catch (CharacterCodingException e) {
                throw new RuntimeException(e);
            }

            var reader = new CSVReader(arrayReader);
            var iterator = reader.iterator();

            // skip header when first partition
            if (partition.index() == 0) {
                iterator.next();
                counter.increment();
            }

            this.rows = new HashSet<>();
            while (iterator.hasNext()) {
                var line = iterator.next();
                rows.add(rowMapper.apply(line));
                counter.increment();
            }
        }

        private CharArrayReader convertToCharArrayReader(Partition partition) throws CharacterCodingException {
            // Prepare the CharsetDecoder
            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();

            // Decode the ByteBuffer directly into a CharBuffer
            CharBuffer charBuffer = decoder.decode(partition.buff());

            // Get the underlying char array if possible
            char[] charArray;
            if (charBuffer.hasArray()) {
                charArray = charBuffer.array();
            } else {
                // Create a new array if the CharBuffer does not provide access to the underlying array
                charArray = new char[charBuffer.remaining()];
                charBuffer.get(charArray);
            }

            // Create the CharArrayReader using the provided offset and length
            return new CharArrayReader(charArray, partition.offset(), partition.length());
        }

        Set<T> rows() {
            return rows;
        }
    }

    private static final class PartitionSpliterator {

        private final Partition[] partitions;
        private int numberOfPartitions;

        private PartitionSpliterator(Path file) throws IOException {
            this.numberOfPartitions = Runtime.getRuntime().availableProcessors();
            var fileSize = Files.size(file);
            var partitionsSize = fileSize / numberOfPartitions;

            if (partitionsSize <= (1 << 8)) { // small partitions: 1 is enough
                numberOfPartitions = 1;
            }

            partitions = new Partition[numberOfPartitions];

            var pos = 0L;
            for (var index = 0; index < numberOfPartitions - 1; index++) {
                try (var channel = (FileChannel) Files.newByteChannel(file, READ)) {
                    var buff = channel.map(READ_ONLY, pos, partitionsSize);
                    pos = normalize(buff, (int) partitionsSize - 1, pos);
                    partitions[index] = new Partition(index, buff, buff.position(), buff.limit());
                }
            }

            // handle last segment
            try (var channel = (FileChannel) Files.newByteChannel(file, READ)) {
                var buff = channel.map(READ_ONLY, pos, fileSize - pos);
                partitions[numberOfPartitions - 1] = new Partition(numberOfPartitions - 1, buff, buff.position(), buff.limit());

            }
        }

        private long normalize(ByteBuffer buff, int relativePos, long pos) {
            while (buff.get(relativePos) != '\n') {
                relativePos--;
            }

            buff.limit(relativePos + 1);
            return pos + (relativePos + 1);
        }

        Partition partition(int index) {
            return partitions[index];
        }

        int numberOfPartitions() {
            return numberOfPartitions;
        }
    }

    record Partition(int index, ByteBuffer buff, int offset, int length) {
    }
}
