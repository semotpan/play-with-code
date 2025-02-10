package io.springbootsamples.data;

public record Person(Long id, String firstName, int age, Boolean active, Integer matchRatePercentage) {
}
