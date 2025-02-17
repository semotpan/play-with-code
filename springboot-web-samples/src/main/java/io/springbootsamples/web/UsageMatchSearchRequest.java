package io.springbootsamples.web;

import lombok.Data;

import java.time.Year;

@Data
public class UsageMatchSearchRequest {
    private String source;
    private String sourceComparison; // if not null, always used "contains",
    private Boolean reconciled;
    private String reconciledComparison;  // if not null, always used "EQ"
    private Year allocationPeriod;
    private String allocationPeriodComparison; // accepts: "eq", "gt", "gte", "lt", "lte"
    private Year startAllocationPeriod;
    private Year endAllocationPeriod;
    private Long totalMatchedUsage;
    private String totalMatchedUsageComparison; // accepts: "eq", "gt", "gte", "lt", "lte"
    private Long startTotalMatchedUsage;
    private Long endTotalMatchedUsage;
    private Long openMatchedUsage;
    private String openMatchedUsageComparison; // accepts: "eq", "gt", "gte", "lt", "lte"
    private Long startOpenMatchedUsage;
    private Long endOpenMatchedUsage;
    private Integer matchRange;
    private String matchRangeComparison; // accepts: "eq", "gt", "gte", "lt", "lte"
    private Integer startMatchRange;
    private Integer endMatchRange;
}
