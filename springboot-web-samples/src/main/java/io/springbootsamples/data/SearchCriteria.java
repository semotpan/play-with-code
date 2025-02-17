package io.springbootsamples.data;

import lombok.Builder;
import lombok.ToString;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Pageable;

import java.time.Year;

import static io.springbootsamples.data.SearchCriteria.ComparisonOperator.CONTAINS;
import static io.springbootsamples.data.SearchCriteria.ComparisonOperator.EQ;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

@ToString
public final class SearchCriteria {

    public final StringCriteria source;
    public final BooleanCriteria reconciled;
    public final YearCriteria allocationPeriod;
    public final NumberCriteria totalMatchedUsage;
    public final NumberCriteria openMatchedUsage;
    public final NumberCriteria matchRange;
    public final Pageable pageable;

    @Builder
    public SearchCriteria(StringCriteria source,
                          BooleanCriteria reconciled,
                          YearCriteria allocationPeriod,
                          NumberCriteria totalMatchedUsage,
                          NumberCriteria openMatchedUsage,
                          NumberCriteria matchRange,
                          Pageable pageable) {
        this.source = requireNonNull(source);
        this.reconciled = requireNonNull(reconciled);
        this.allocationPeriod = requireNonNull(allocationPeriod);
        this.totalMatchedUsage = requireNonNull(totalMatchedUsage);
        this.openMatchedUsage = requireNonNull(openMatchedUsage);
        this.matchRange = requireNonNull(matchRange);
        this.pageable = requireNonNull(pageable);
    }

    public boolean anyFilterCriteria() {
        return source.isValid() || reconciled.isValid() || allocationPeriod.isValid() || totalMatchedUsage.isValid();
    }

    @ToString
    public static final class BooleanCriteria {

        public final ComparisonOperator operator;
        public final Boolean value;

        @Builder
        public BooleanCriteria(String operator, Boolean value) {
            this.operator = Strings.isBlank(operator) ? null : EQ;
            this.value = value;
        }

        public boolean isValid() {
            return nonNull(operator);
        }
    }

    @ToString
    public static final class StringCriteria {

        public final ComparisonOperator operator;
        public final String value;

        @Builder
        public StringCriteria(String operator, String value) {
            this.operator = Strings.isBlank(operator) ? null : CONTAINS;
            this.value = value;
        }

        public boolean isValid() {
            return nonNull(operator);
        }
    }

    @ToString
    public static final class YearCriteria {

        public final ComparisonOperator operator;
        public final Year value;
        public final Year startValue;
        public final Year endValue;

        @Builder
        public YearCriteria(String operator, Year value, Year startValue, Year endValue) {
            if (nonNull(startValue) && nonNull(endValue)) {
                this.operator = ComparisonOperator.RANGE;
                this.startValue = startValue;
                this.endValue = startValue;
                this.value = null;
                return;
            }

            this.startValue = null;
            this.endValue = null;
            this.operator = ComparisonOperator.isValid(operator) ? ComparisonOperator.fromString(operator) : null;
            this.value = value;
        }

        public boolean isValid() {
            return (nonNull(startValue) && nonNull(endValue)) || (nonNull(operator) && nonNull(value));
        }

        public boolean isRange() {
            return nonNull(startValue) && nonNull(endValue);
        }
    }

    @ToString
    public static final class NumberCriteria {

        public final ComparisonOperator operator;
        public final Number value;
        public final Number startValue;
        public final Number endValue;

        @Builder
        public NumberCriteria(String operator, Number value, Number startValue, Number endValue) {
            if (nonNull(startValue) && nonNull(endValue)) {
                this.operator = ComparisonOperator.RANGE;
                this.startValue = startValue;
                this.endValue = endValue;
                this.value = null;
                return;
            }

            this.startValue = null;
            this.endValue = null;
            this.operator = ComparisonOperator.isValid(operator) ? ComparisonOperator.fromString(operator) : null;
            this.value = value;
        }

        public boolean isRange() {
            return nonNull(startValue) && nonNull(endValue);
        }

        public boolean isValid() {
            return (nonNull(startValue) && nonNull(endValue)) || (nonNull(operator) && nonNull(value));
        }
    }

    public enum ComparisonOperator {
        EQ("eq"),
        GT("gt"),
        GTE("gte"),
        LT("lt"),
        LTE("lte"),
        RANGE("range"),
        CONTAINS("contains");

        public final String operator;

        ComparisonOperator(String operator) {
            this.operator = operator;
        }

        public static boolean isValid(String value) {
            return nonNull(fromString(value));
        }

        public static ComparisonOperator fromString(String operator) {
            for (ComparisonOperator comparisonOperator : ComparisonOperator.values()) {
                if (comparisonOperator.operator.equalsIgnoreCase(operator)) {
                    return comparisonOperator;
                }
            }

            return null;
        }
    }
}
