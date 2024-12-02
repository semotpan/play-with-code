package io.example;

import java.util.Comparator;

public record Employee(Integer id, String firstName, String lastName) implements Comparable<Employee> {

    @Override
    public int compareTo(Employee o) {
        return COMPARATOR.compare(this, o);
    }

    // Comparator that handles nulls in firstName and lastName
    public static final Comparator<Employee> COMPARATOR = Comparator
            .comparing(Employee::lastName, Comparator.nullsFirst(String::compareTo))
            .thenComparing(Employee::firstName, Comparator.nullsFirst(String::compareTo));
}
