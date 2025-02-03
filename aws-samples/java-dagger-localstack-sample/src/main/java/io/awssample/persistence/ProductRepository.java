package io.awssample.persistence;

import io.awssample.domain.Product;

import java.util.Optional;

public interface ProductRepository {

    Optional<Product> findById(String id);

    void put(Product product);

}
