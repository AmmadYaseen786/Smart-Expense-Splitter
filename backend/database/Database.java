package database;

import java.sql.Connection;
import java.sql.DriverManager;

public class Database {

    // Database URL
    private static final String URL =
            "jdbc:mysql://localhost:3306/expense_splitter";

    // MySQL username
    private static final String USER = "root";

    // MySQL password
    private static final String PASSWORD = "root";

    // Connect method
    public static Connection connect() {

        try {

            Connection conn = DriverManager.getConnection(
                    URL,
                    USER,
                    PASSWORD
            );

            System.out.println("Database connected!");

            return conn;

        } catch (Exception e) {

            System.out.println("Database connection failed!");

            e.printStackTrace();

            return null;
        }
    }
}