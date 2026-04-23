package com.springbooters.recruitment.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

/**
 * Central JDBC connection helper for the recruitment subsystem.
 *
 * Configure using environment variables:
 * DB_URL, DB_USER and DB_PASSWORD.
 */
public class DatabaseConnection {

    private static final String DEFAULT_URL = "jdbc:sqlite:database/hrms.db";
    private static final String DEFAULT_USER = "";
    private static final String DEFAULT_PASSWORD = "";
    
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found in classpath!");
        }
    }

    private final String url;
    private final String user;
    private final String password;
    private static boolean isInitialized = false;

    public DatabaseConnection() {
        this(System.getenv().getOrDefault("DB_URL", DEFAULT_URL),
                System.getenv().getOrDefault("DB_USER", DEFAULT_USER),
                System.getenv().getOrDefault("DB_PASSWORD", DEFAULT_PASSWORD));
    }

    public DatabaseConnection(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private void initializeDatabase() {
        if (isInitialized) return;
        
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            
            String schemaPath = "database/schema.sql";
            if (Files.exists(Paths.get(schemaPath))) {
                String content = new String(Files.readAllBytes(Paths.get(schemaPath)));
                for (String sql : content.split(";")) {
                    String trimmed = sql.trim();
                    // Skip MySQL specific commands
                    if (!trimmed.isEmpty() && !trimmed.toUpperCase().startsWith("CREATE DATABASE") 
                        && !trimmed.toUpperCase().startsWith("USE ")) {
                        stmt.execute(trimmed);
                    }
                }
                isInitialized = true;
            }
        } catch (Exception e) {
            System.err.println("Database auto-initialization failed: " + e.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        initializeDatabase();
        return DriverManager.getConnection(url);
    }
}
