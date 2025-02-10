package io.springbootsamples.web;

import io.springbootsamples.data.ProductData;
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
@RequestMapping(value = "/v1/people")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {GET, POST, PUT, DELETE})
final class PeopleController {

    private final ProductData productData;

    @GetMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    ResponseEntity<?> filtering(@RequestParam(required = false) Long id,
                                @RequestParam(required = false) String idComparison, // Accepts "eq", "gt", "gte", "lt", "lte" => skip "contains" use default "eq"
                                @RequestParam(required = false) String firstName,
                                @RequestParam(required = false) String firstNameComparison, // Name contains filter, case-insensitive
                                @RequestParam(required = false) Integer startAge,      // Age >= start, if the end not present, take all >= startAge
                                @RequestParam(required = false) Integer endAge,      // Age <= end, if the start not present, take all <= endAge
                                Pageable pageable) {
        var filterData = FilterData.builder()
                .id(id)
                .idComparison(idComparison)
                .firstName(firstName)
                .firstNameComparison(firstNameComparison)
                .startAge(startAge)
                .endAge(endAge)
                .pageable(pageable)
                .build();

        log.info("Input request: {}", filterData);

        return ResponseEntity.ok()
                .body(productData.find(filterData));

    }

    @GetMapping(path = "/all", produces = APPLICATION_JSON_VALUE)
    ResponseEntity<?> all() {
        return ResponseEntity.ok()
                .body(productData.values());
    }
}
