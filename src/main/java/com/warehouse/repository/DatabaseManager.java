package com.warehouse.repository;

import com.warehouse.model.Product;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;

    private final String url = "jdbc:mysql://localhost:3306/db01";
    private final String username = "root";
    private final String password = "";

    public DatabaseManager() {
    }

    public static DatabaseManager getInstance() throws SQLException {
        if (instance == null || instance.getConnection() == null || instance.getConnection().isClosed()) {
            instance = new DatabaseManager();
            instance.connect();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(url, username, password);
            clearProductsTable();
            System.out.println("Database connected successfully.");
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC Driver not found!", e);
        }
    }

    private void clearProductsTable() throws SQLException {
        String query = "DELETE FROM Products";
        executeUpdate(query);
    }

    public int executeUpdate(String query, Object... params) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            setParameters(stmt, params);
            return stmt.executeUpdate();
        }
    }

    private void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Failed to close connection: " + e.getMessage());
            }
        }
    }

    public void addProduct(Product product) throws SQLException {
        String query = "INSERT INTO Products (Name, Category, GroupID, LocationX, LocationY, Weight) VALUES (?, ?, ?, ?, ?, ?)";
        executeUpdate(query, product.getName(), product.getCategory(), product.getGroupId(),
                product.getLocationX(), product.getLocationY(), product.getWeight());
        System.out.println("Product added: " + product.getName());
    }

    public void deleteProduct(String name, String category, int locationX, int locationY) throws SQLException {
        System.out.println("Deleting product: " + name + ", " + category + ", " + locationX + ", " + locationY);
        String query = "DELETE FROM Products WHERE Name = ? AND Category = ? AND LocationX = ? AND LocationY = ?";
        int rowsAffected = executeUpdate(query, name, category, locationX, locationY);
        if (rowsAffected > 0) {
            System.out.println("Product deleted successfully.");
        } else {
            System.out.println("No product found with the specified criteria.");
        }
    }
}
