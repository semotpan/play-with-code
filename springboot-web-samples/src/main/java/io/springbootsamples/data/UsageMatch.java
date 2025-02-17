package io.springbootsamples.data;

import java.time.Year;
import java.util.UUID;

public record UsageMatch(UUID id,
                         String source,
                         Boolean reconciled,
                         Year allocationPeriod,
                         Long totalMatchedUsage,
                         Long openMatchedUsage,
                         Integer matchRange) {
}
