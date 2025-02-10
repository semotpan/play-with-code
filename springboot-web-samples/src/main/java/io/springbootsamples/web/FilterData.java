package io.springbootsamples.web;

import lombok.Builder;
import org.springframework.data.domain.Pageable;

@Builder
public record FilterData(Long id,
                         String idComparison,
                         String firstName,
                         String firstNameComparison,
                         Integer startAge,
                         Integer endAge,
                         Pageable pageable) {
}
