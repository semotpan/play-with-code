package io.springbootsamples.web;

import io.springbootsamples.data.SearchCriteria;
import io.springbootsamples.data.UsageMatchQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/v1/usage-matches")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {GET, POST, PUT, DELETE})
final class UsageMatchResource {

    private final UsageMatchQuery usageMatchQuery;

    @GetMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    ResponseEntity<?> search(@ModelAttribute UsageMatchSearchRequest request, Pageable pageable) {

        var searchCriteria = SearchCriteria.builder()
                .source(SearchCriteria.StringCriteria.builder()
                        .value(request.getSource())
                        .operator(request.getSourceComparison())
                        .build())
                .reconciled(SearchCriteria.BooleanCriteria.builder()
                        .value(request.getReconciled())
                        .operator(request.getReconciledComparison())
                        .build())
                .allocationPeriod(SearchCriteria.YearCriteria.builder()
                        .value(request.getAllocationPeriod())
                        .operator(request.getAllocationPeriodComparison())
                        .startValue(request.getStartAllocationPeriod())
                        .endValue(request.getEndAllocationPeriod())
                        .build())
                .totalMatchedUsage(SearchCriteria.NumberCriteria.builder()
                        .value(request.getTotalMatchedUsage())
                        .operator(request.getTotalMatchedUsageComparison())
                        .startValue(request.getStartTotalMatchedUsage())
                        .endValue(request.getEndTotalMatchedUsage())
                        .build())
                .openMatchedUsage(SearchCriteria.NumberCriteria.builder()
                        .value(request.getOpenMatchedUsage())
                        .operator(request.getOpenMatchedUsageComparison())
                        .startValue(request.getStartOpenMatchedUsage())
                        .endValue(request.getEndOpenMatchedUsage())
                        .build())
                .matchRange(SearchCriteria.NumberCriteria.builder()
                        .value(request.getMatchRange())
                        .operator(request.getMatchRangeComparison())
                        .startValue(request.getStartMatchRange())
                        .endValue(request.getEndMatchRange())
                        .build())
                .pageable(pageable)
                .build();

        log.info("Executing search with criteria: {}", searchCriteria);

        return ResponseEntity.ok(usageMatchQuery.find(searchCriteria));
    }
}
