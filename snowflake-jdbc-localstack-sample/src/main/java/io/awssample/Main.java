package io.awssample;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    static {
        try {
            Class.forName("net.snowflake.client.jdbc.SnowflakeDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        SnowflakeModule snowflakeModule = new SnowflakeModule();
        try (HikariDataSource dataSource = snowflakeModule.provideHikariDataSource();
             Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery("SELECT name, price, id FROM product");

            while (resultSet.next()) {
                var id = resultSet.getString("ID");
                var name = resultSet.getString("NAME");
                var price = resultSet.getBigDecimal("PRICE");
                System.out.printf("ID: %s, Name: %s, Created At: %s%n", id, name, price);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
