package io.springbootsamples.data;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

import static org.apache.logging.log4j.util.Strings.isBlank;

@Component
@RequiredArgsConstructor
public class UsageMatchQuery {

    private final InMemoryDatasource inMemoryDatasource;

    public Page<UsageMatch> find(SearchCriteria criteria) {
        Objects.requireNonNull(criteria);

        // Step 1: Apply filters
        var filtered = new ArrayList<>(inMemoryDatasource.values().stream()
                .filter(Objects::nonNull)
                .filter(value -> sourceFilter(criteria, value))
                .filter(value -> reconciledFilter(criteria, value))
                .filter(value -> allocationPeriodFilter(criteria, value))
                .filter(value -> totalMatchedUsageFilter(criteria, value))
                .filter(value -> openMatchedUsageFilter(criteria, value))
                .filter(value -> matchRangeFilter(criteria, value))
                .toList());

        // Step 2: Apply Sorting if Present
        var sort = criteria.pageable.getSort();
        if (sort.isSorted()) {
            Comparator<UsageMatch> comparator = null;
            for (var order : sort) {
                var fieldComparator = getComparator(order);
                if (order.isDescending()) {
                    fieldComparator = fieldComparator.reversed();
                }
                comparator = (comparator == null) ? fieldComparator : comparator.thenComparing(fieldComparator);
            }
            filtered.sort(comparator);
        }


        // Step 3: Paginate the filtered and sorted list
        var start = (int) criteria.pageable.getOffset();
        var end = Math.min(start + criteria.pageable.getPageSize(), filtered.size());

        var pageContent = filtered.subList(start, end);

        return new PageImpl<>(pageContent, criteria.pageable, filtered.size());

    }

    static boolean sourceFilter(SearchCriteria searchCriteria, UsageMatch usageMatch) {
        if (!searchCriteria.source.isValid()) {
            return true;
        }

        return !isBlank(searchCriteria.source.value) && usageMatch.source().toLowerCase().contains(searchCriteria.source.value.toLowerCase());
    }

    static boolean reconciledFilter(SearchCriteria searchCriteria, UsageMatch usageMatch) {
        if (!searchCriteria.reconciled.isValid()) {
            return true;
        }

        return usageMatch.reconciled().equals(searchCriteria.reconciled.value);
    }


    static boolean allocationPeriodFilter(SearchCriteria searchCriteria, UsageMatch usageMatch) {
        if (!searchCriteria.allocationPeriod.isValid()) {
            return true;
        }

        if (searchCriteria.allocationPeriod.isRange()) {
            return !searchCriteria.allocationPeriod.startValue.isAfter(usageMatch.allocationPeriod()) &&
                    !searchCriteria.allocationPeriod.endValue.isBefore(usageMatch.allocationPeriod());
        }

        return switch (searchCriteria.allocationPeriod.operator) {
            case EQ -> usageMatch.allocationPeriod().equals(searchCriteria.allocationPeriod.value);
            case GT -> usageMatch.allocationPeriod().isAfter(searchCriteria.allocationPeriod.value);
            case LT -> usageMatch.allocationPeriod().isBefore(searchCriteria.allocationPeriod.value);
            case GTE -> !usageMatch.allocationPeriod().isBefore(searchCriteria.allocationPeriod.value);
            case LTE -> !usageMatch.allocationPeriod().isAfter(searchCriteria.allocationPeriod.value);
            default -> false;
        };
    }

    static boolean totalMatchedUsageFilter(SearchCriteria searchCriteria, UsageMatch usageMatch) {
        if (!searchCriteria.totalMatchedUsage.isValid()) {
            return true;
        }

        if (searchCriteria.totalMatchedUsage.isRange()) {
            return searchCriteria.totalMatchedUsage.startValue.longValue() <= usageMatch.totalMatchedUsage() &&
                    usageMatch.totalMatchedUsage() <= searchCriteria.totalMatchedUsage.endValue.longValue();
        }

        return switch (searchCriteria.totalMatchedUsage.operator) {
            case EQ -> Objects.equals(searchCriteria.totalMatchedUsage.value, usageMatch.totalMatchedUsage());
            case GT -> searchCriteria.totalMatchedUsage.value.longValue() < usageMatch.totalMatchedUsage();
            case LT -> searchCriteria.totalMatchedUsage.value.longValue() > usageMatch.totalMatchedUsage();
            case GTE -> searchCriteria.totalMatchedUsage.value.longValue() <= usageMatch.totalMatchedUsage();
            case LTE -> searchCriteria.totalMatchedUsage.value.longValue() >= usageMatch.totalMatchedUsage();
            default -> false;
        };
    }

    static boolean matchRangeFilter(SearchCriteria searchCriteria, UsageMatch usageMatch) {
        if (!searchCriteria.matchRange.isValid()) {
            return true;
        }

        if (searchCriteria.matchRange.isRange()) {
            return searchCriteria.matchRange.startValue.intValue() <= usageMatch.matchRange() &&
                    usageMatch.matchRange() <= searchCriteria.matchRange.endValue.intValue();
        }

        return switch (searchCriteria.matchRange.operator) {
            case EQ -> Objects.equals(searchCriteria.matchRange.value.intValue(), usageMatch.matchRange());
            case GT -> searchCriteria.matchRange.value.intValue() < usageMatch.matchRange();
            case LT -> searchCriteria.matchRange.value.intValue() > usageMatch.matchRange();
            case GTE -> searchCriteria.matchRange.value.intValue() <= usageMatch.matchRange();
            case LTE -> searchCriteria.matchRange.value.intValue() >= usageMatch.matchRange();
            default -> false;
        };
    }

    static boolean openMatchedUsageFilter(SearchCriteria searchCriteria, UsageMatch usageMatch) {
        if (!searchCriteria.openMatchedUsage.isValid()) {
            return true;
        }

        if (searchCriteria.openMatchedUsage.isRange()) {
            return searchCriteria.openMatchedUsage.startValue.longValue() <= usageMatch.openMatchedUsage() &&
                    usageMatch.openMatchedUsage() <= searchCriteria.openMatchedUsage.endValue.longValue();
        }

        return switch (searchCriteria.openMatchedUsage.operator) {
            case EQ -> Objects.equals(searchCriteria.openMatchedUsage.value, usageMatch.openMatchedUsage());
            case GT -> searchCriteria.openMatchedUsage.value.longValue() < usageMatch.openMatchedUsage();
            case LT -> searchCriteria.openMatchedUsage.value.longValue() > usageMatch.openMatchedUsage();
            case GTE -> searchCriteria.openMatchedUsage.value.longValue() <= usageMatch.openMatchedUsage();
            case LTE -> searchCriteria.openMatchedUsage.value.longValue() >= usageMatch.openMatchedUsage();
            default -> false;
        };
    }


    private Comparator<UsageMatch> getComparator(Sort.Order order) {
        return switch (order.getProperty()) {
            case "source" -> Comparator.comparing(UsageMatch::source, String.CASE_INSENSITIVE_ORDER);
            case "reconciled" -> Comparator.comparing(UsageMatch::reconciled);
            case "allocationPeriod" -> Comparator.comparing(UsageMatch::allocationPeriod);
            case "totalMatchedUsage" -> Comparator.comparing(UsageMatch::totalMatchedUsage);
            case "openMatchedUsage" -> Comparator.comparing(UsageMatch::openMatchedUsage);
            case "matchRange" -> Comparator.comparing(UsageMatch::matchRange);
            default -> throw new IllegalArgumentException("Unknown sorting field: " + order.getProperty());
        };
    }
}
