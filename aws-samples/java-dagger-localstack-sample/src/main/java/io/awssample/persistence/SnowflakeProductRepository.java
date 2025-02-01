package io.awssample.persistence;

import io.awssample.domain.Product;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;

public class SnowflakeProductRepository implements ProductRepository {

    private final HikariDataSource dataSource;

    public SnowflakeProductRepository(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Product> findById(String id) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM product WHERE id=%s".formatted(id))) {

            while (resultSet.next()) {
                var id1 = resultSet.getString("id");
                var name = resultSet.getString("name");
                var price = resultSet.getBigDecimal("price");
                System.out.printf("ID: %d, Name: %s, Created At: %s%n", id1, name, price);
                return Optional.of(new Product(id1, name, price));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public void put(Product product) {

    }
}
