package io.springbootsamples.data;

import io.springbootsamples.web.FilterData;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class ProductData {

    private static final Logger log = LoggerFactory.getLogger(ProductData.class);
    private final List<Person> people = new ArrayList<>();

    @PostConstruct
    public void loadProducts() {
        people.add(new Person(1L, "Alice", 25));
        people.add(new Person(2L, "Bob", 30));
        people.add(new Person(3L, "Charlie", 22));
        people.add(new Person(4L, "David", 35));
        people.add(new Person(5L, "Eva", 28));
        people.add(new Person(6L, "Frank", 40));
        people.add(new Person(7L, "Grace", 19));
        people.add(new Person(8L, "Henry", 27));
        people.add(new Person(9L, "Isabella", 33));
        people.add(new Person(10L, "Jack", 45));
        people.add(new Person(11L, "Karen", 29));
        people.add(new Person(12L, "Leo", 50));
        people.add(new Person(13L, "Mia", 26));
        people.add(new Person(14L, "Nathan", 23));
        people.add(new Person(15L, "Olivia", 31));
        people.add(new Person(16L, "Paul", 41));
        people.add(new Person(17L, "Quinn", 38));
        people.add(new Person(18L, "Rachel", 24));
        people.add(new Person(19L, "Steve", 37));
        people.add(new Person(20L, "Tina", 21));
        people.add(new Person(21L, "Umar", 48));
        people.add(new Person(22L, "Vera", 36));
        people.add(new Person(23L, "Will", 32));
        people.add(new Person(24L, "Xena", 44));
        people.add(new Person(25L, "Yusuf", 39));
        people.add(new Person(26L, "Zara", 20));
        people.add(new Person(27L, "Ethan", 42));
        people.add(new Person(28L, "Sophia", 34));
        people.add(new Person(29L, "Daniel", 46));
        people.add(new Person(30L, "Emma", 43));
    }

    public List<Person> values() {
        return people;
    }

    public Page<Person> find(FilterData filterData) {

        if (filterData.firstNameComparison() == null || filterData.firstNameComparison().isEmpty() || !"contains".equalsIgnoreCase(filterData.firstNameComparison())) {
            log.info("firstNameComparison: {}, is invalid, apply 'contains' case-insensitive strategy", filterData.firstNameComparison());
        }

        // Step 1: Apply filters
        List<Person> list = people.stream()
                .filter(person -> applyIdFilter(person, filterData.id(), filterData.idComparison()))
                .filter(person -> applyFirstNameFilter(person, filterData.firstName()))
                .filter(person -> applyAgeFilter(person, filterData.startAge(), filterData.endAge()))
                .toList();

        var filteredList = new ArrayList<>(list);

        // Step 2: Apply Sorting if Present
        Sort sort = filterData.pageable().getSort();
        if (sort.isSorted()) {
            Comparator<Person> comparator = null;
            for (Sort.Order order : sort) {
                Comparator<Person> fieldComparator = getComparator(order);
                if (order.isDescending()) {
                    fieldComparator = fieldComparator.reversed();
                }
                comparator = (comparator == null) ? fieldComparator : comparator.thenComparing(fieldComparator);
            }
            filteredList.sort(comparator);
        }

        // Step 3: Paginate the filtered and sorted list
        int start = (int) filterData.pageable().getOffset();
        int end = Math.min(start + filterData.pageable().getPageSize(), filteredList.size());

        List<Person> pageContent = filteredList.subList(start, end);

        return new PageImpl<>(pageContent, filterData.pageable(), filteredList.size());
    }

    private boolean applyIdFilter(Person person, Long id, String idComparison) {
        if (id == null || idComparison == null) return true;

        return switch (idComparison) {
            case "eq" -> person.id().equals(id);
            case "gt" -> person.id() > id;
            case "gte" -> person.id() >= id;
            case "lt" -> person.id() < id;
            case "lte" -> person.id() <= id;
            default -> true;
        };
    }

    private boolean applyFirstNameFilter(Person person, String firstName) {
        return firstName == null || person.firstName().toLowerCase().contains(firstName.toLowerCase());
    }

    private boolean applyAgeFilter(Person person, Integer minAge, Integer maxAge) {
        return (minAge == null || person.age() >= minAge) && (maxAge == null || person.age() <= maxAge);
    }

    private Comparator<Person> getComparator(Sort.Order order) {
        return switch (order.getProperty()) {
            case "id" -> Comparator.comparing(Person::id);
            case "firstName" -> Comparator.comparing(Person::firstName, String.CASE_INSENSITIVE_ORDER);
            case "age" -> Comparator.comparing(Person::age);
            default -> throw new IllegalArgumentException("Unknown sorting field: " + order.getProperty());
        };
    }
}
