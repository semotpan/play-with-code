package io.example;

import io.example.db.Datasource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

public class EmployeeRepository {

    static final String INSERT_SQL = "INSERT INTO employees(first_name, last_name) VALUES (?,?)";
    static final String UPDATE_SQL = "UPDATE employees SET first_name=?, last_name=? WHERE id=?";

    public void add(Collection<Employee> employees) {
        try (Connection conn = Datasource.getConnection()) {
            try {
                PreparedStatement employeeStmt = conn.prepareStatement(INSERT_SQL);
                //Assume a valid connection object conn
                conn.setAutoCommit(false);

                for (Employee employee : employees) {
                    employeeStmt.setString(1, employee.firstName());
                    employeeStmt.setString(2, employee.lastName());
                    employeeStmt.addBatch();
                }

                employeeStmt.executeBatch();

                // If there is no error.
                conn.commit();
            } catch (SQLException se) {
                // If there is any error.
                try {
                    conn.rollback();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(Collection<Employee> employees) {
        try (Connection conn = Datasource.getConnection()) {
            try {
                PreparedStatement employeeStmt = conn.prepareStatement(UPDATE_SQL);
                //Assume a valid connection object conn
                conn.setAutoCommit(false);

                for (Employee employee : employees) {
                    employeeStmt.setString(1, employee.firstName());
                    employeeStmt.setString(2, employee.lastName());
                    employeeStmt.setInt(3, employee.id());
                    employeeStmt.addBatch();
                }

                employeeStmt.executeBatch();

                // If there is no error.
                conn.commit();
            } catch (SQLException se) {
                // If there is any error.
                try {
                    conn.rollback();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
