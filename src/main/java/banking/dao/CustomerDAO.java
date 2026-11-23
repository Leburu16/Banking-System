package banking.dao;

import banking.entity.Customer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** All JDBC code for customers. */
public final class CustomerDAO {

    public void insert(Connection conn, Customer c) throws SQLException {
        String sql = "INSERT INTO customers (customer_id, first_name, surname, address, phone, email) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getCustomerId());
            ps.setString(2, c.getFirstName());
            ps.setString(3, c.getSurname());
            ps.setString(4, c.getAddress());
            ps.setString(5, c.getPhone());
            ps.setString(6, c.getEmail());
            ps.executeUpdate();
        }
    }

    /** Finds the customer who owns this login (CustomerDAO.findCustomer in the sequence diagram). */
    public Optional<Customer> findCustomer(Connection conn, String username) throws SQLException {
        String sql = "SELECT c.customer_id, c.first_name, c.surname, c.address, c.phone, c.email "
                + "FROM customers c JOIN users u ON u.customer_id = c.customer_id WHERE u.username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    private static Customer map(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getString("customer_id"),
                rs.getString("first_name"),
                rs.getString("surname"),
                rs.getString("address"),
                rs.getString("phone"),
                rs.getString("email"));
    }
}
