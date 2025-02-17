package io.springbootsamples.data;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
final class InMemoryDatasource {

    private final List<UsageMatch> values = new ArrayList<>(200);
    private final Random random = new Random(12345); // Fixed seed for reproducibility

    @PostConstruct
    void init() {
        for (int i = 0; i < 200; i++) {
            values.add(generateDeterministicUsageMatch(i));
        }
    }

    List<UsageMatch> values() {
        return values;
    }

    private UsageMatch generateDeterministicUsageMatch(int index) {
        return new UsageMatch(
                generateFixedUUID(index),
                getFixedRadioStation(index),
                index % 2 == 0, // Alternates between true and false
                Year.of(index % 2 == 0 ? 2023 : 2024), // Alternates between 2023 and 2024
                getFixedLong(index, 1000, 1_000_000_000),
                getFixedLong(index + 1, 1000, 1_000_000_000),
                index % 101 // Cycles between 0 and 100
        );
    }

    private UUID generateFixedUUID(int index) {
        return UUID.nameUUIDFromBytes(("fixed-seed-" + index).getBytes());
    }

    private String getFixedRadioStation(int index) {
        String[] stations = {"Radio One", "Jazz FM", "Classic Rock", "News Channel", "Pop Hits",
                "Rock FM", "Smooth Vibes", "Hip-Hop Nation", "Electro Beats", "Indie Waves",
                "Country Gold", "Reggae Groove", "Soul Classics", "Folk Tunes", "EDM Essentials",
                "Latin Fiesta", "Blues Express", "Metal Mayhem", "Retro 80s", "90s Nostalgia",
                "K-Pop Central", "Bollywood Beats", "Classical Symphony", "Talk Radio", "Gospel Hour",
                "Techno Pulse", "Trance Flow", "Lo-Fi Lounge", "Alternative Edge", "Hard Rock Legends"};
        return stations[index % stations.length]; // Cycles through the list
    }

    private Long getFixedLong(int index, long min, long max) {
        return min + (Math.abs(random.nextInt()) % (max - min)); // Fixed random-like long value
    }
}
