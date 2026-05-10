package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import database.Database;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class SimpleServer {

    public static void main(String[] args) throws Exception {

        // Connect database
        Database.connect();

        // Create server
        HttpServer server = HttpServer.create(
                new InetSocketAddress(8080),
                0
        );

        // Home route
        server.createContext("/", exchange -> {

            String response = "Expense Splitter Server Running!";

            exchange.sendResponseHeaders(200, response.getBytes().length);

            OutputStream os = exchange.getResponseBody();

            os.write(response.getBytes());

            os.close();
        });

        // REGISTER API
        server.createContext("/register", exchange -> {

            // Only allow POST method
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {

                // Read request body
                InputStreamReader isr = new InputStreamReader(
                        exchange.getRequestBody(),
                        StandardCharsets.UTF_8
                );

                BufferedReader br = new BufferedReader(isr);

                StringBuilder requestBody = new StringBuilder();

                String line;

                while ((line = br.readLine()) != null) {
                    requestBody.append(line);
                }

                // Example body:
                // name=Ammad&email=test@gmail.com&password=123

                String body = requestBody.toString();

                // Split data
                String[] pairs = body.split("&");

                String name = pairs[0].split("=")[1];
                String email = pairs[1].split("=")[1];
                String password = pairs[2].split("=")[1];

                try {

                    Connection conn = Database.connect();

                    String sql =
                            "INSERT INTO users(name, email, password) VALUES (?, ?, ?)";

                    PreparedStatement stmt =
                            conn.prepareStatement(sql);

                    stmt.setString(1, name);
                    stmt.setString(2, email);
                    stmt.setString(3, password);

                    stmt.executeUpdate();

                    String response = "User registered successfully!";

                    exchange.sendResponseHeaders(
                            200,
                            response.getBytes().length
                    );

                    OutputStream os = exchange.getResponseBody();

                    os.write(response.getBytes());

                    os.close();

                    System.out.println("User registered!");

                } catch (Exception e) {

                    String response = "Registration failed!";

                    exchange.sendResponseHeaders(
                            500,
                            response.getBytes().length
                    );

                    OutputStream os = exchange.getResponseBody();

                    os.write(response.getBytes());

                    os.close();

                    e.printStackTrace();
                }

            } else {

                String response = "Only POST method allowed";

                exchange.sendResponseHeaders(
                        405,
                        response.getBytes().length
                );

                OutputStream os = exchange.getResponseBody();

                os.write(response.getBytes());

                os.close();
            }
        });

        // LOGIN API
server.createContext("/login", exchange -> {

    // Only POST method
    if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {

        // Read request body
        InputStreamReader isr = new InputStreamReader(
                exchange.getRequestBody(),
                StandardCharsets.UTF_8
        );

        BufferedReader br = new BufferedReader(isr);

        StringBuilder requestBody = new StringBuilder();

        String line;

        while ((line = br.readLine()) != null) {
            requestBody.append(line);
        }

        // Example:
        // email=test@gmail.com&password=123

        String body = requestBody.toString();

        String[] pairs = body.split("&");

        String email = pairs[0].split("=")[1];
        String password = pairs[1].split("=")[1];

        try {

            Connection conn = Database.connect();

            String sql =
                    "SELECT * FROM users WHERE email=? AND password=?";

            PreparedStatement stmt =
                    conn.prepareStatement(sql);

            stmt.setString(1, email);
            stmt.setString(2, password);

            var result = stmt.executeQuery();

            String response;

            if (result.next()) {

                response = "Login successful!";

                exchange.sendResponseHeaders(
                        200,
                        response.getBytes().length
                );

            } else {

                response = "Invalid email or password!";

                exchange.sendResponseHeaders(
                        401,
                        response.getBytes().length
                );
            }

            OutputStream os = exchange.getResponseBody();

            os.write(response.getBytes());

            os.close();

        } catch (Exception e) {

            String response = "Login failed!";

            exchange.sendResponseHeaders(
                    500,
                    response.getBytes().length
            );

            OutputStream os = exchange.getResponseBody();

            os.write(response.getBytes());

            os.close();

            e.printStackTrace();
        }

    } else {

        String response = "Only POST method allowed";

        exchange.sendResponseHeaders(
                405,
                response.getBytes().length
        );

        OutputStream os = exchange.getResponseBody();

        os.write(response.getBytes());

        os.close();
    }
});

// CREATE GROUP API
server.createContext("/create-group", exchange -> {

    // Only POST method
    if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {

        // Read request body
        InputStreamReader isr = new InputStreamReader(
                exchange.getRequestBody(),
                StandardCharsets.UTF_8
        );

        BufferedReader br = new BufferedReader(isr);

        StringBuilder requestBody = new StringBuilder();

        String line;

        while ((line = br.readLine()) != null) {
            requestBody.append(line);
        }

        // Example:
        // group_name=Trip Group&created_by=1

        String body = requestBody.toString();

        String[] pairs = body.split("&");

        String groupName = pairs[0].split("=")[1];

        int createdBy =
                Integer.parseInt(pairs[1].split("=")[1]);

        try {

            Connection conn = Database.connect();

            String sql =
                    "INSERT INTO groups_table(group_name, created_by) VALUES (?, ?)";

            PreparedStatement stmt =
                    conn.prepareStatement(sql);

            stmt.setString(1, groupName);

            stmt.setInt(2, createdBy);

            stmt.executeUpdate();

            String response =
                    "Group created successfully!";

            exchange.sendResponseHeaders(
                    200,
                    response.getBytes().length
            );

            OutputStream os = exchange.getResponseBody();

            os.write(response.getBytes());

            os.close();

            System.out.println("Group created!");

        } catch (Exception e) {

            String response = "Failed to create group!";

            exchange.sendResponseHeaders(
                    500,
                    response.getBytes().length
            );

            OutputStream os = exchange.getResponseBody();

            os.write(response.getBytes());

            os.close();

            e.printStackTrace();
        }

    } else {

        String response = "Only POST method allowed";

        exchange.sendResponseHeaders(
                405,
                response.getBytes().length
        );

        OutputStream os = exchange.getResponseBody();

        os.write(response.getBytes());

        os.close();
    }
});

// ADD MEMBER TO GROUP API
server.createContext("/add-member", exchange -> {

    // Only POST method
    if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {

        // Read request body
        InputStreamReader isr = new InputStreamReader(
                exchange.getRequestBody(),
                StandardCharsets.UTF_8
        );

        BufferedReader br = new BufferedReader(isr);

        StringBuilder requestBody = new StringBuilder();

        String line;

        while ((line = br.readLine()) != null) {
            requestBody.append(line);
        }

        // Example:
        // group_id=1&user_id=2

        String body = requestBody.toString();

        String[] pairs = body.split("&");

        int groupId =
                Integer.parseInt(pairs[0].split("=")[1]);

        int userId =
                Integer.parseInt(pairs[1].split("=")[1]);

        try {

            Connection conn = Database.connect();

            String sql =
                    "INSERT INTO group_members(group_id, user_id) VALUES (?, ?)";

            PreparedStatement stmt =
                    conn.prepareStatement(sql);

            stmt.setInt(1, groupId);

            stmt.setInt(2, userId);

            stmt.executeUpdate();

            String response =
                    "Member added successfully!";

            exchange.sendResponseHeaders(
                    200,
                    response.getBytes().length
            );

            OutputStream os = exchange.getResponseBody();

            os.write(response.getBytes());

            os.close();

            System.out.println("Member added!");

        } catch (Exception e) {

            String response = "Failed to add member!";

            exchange.sendResponseHeaders(
                    500,
                    response.getBytes().length
            );

            OutputStream os = exchange.getResponseBody();

            os.write(response.getBytes());

            os.close();

            e.printStackTrace();
        }

    } else {

        String response = "Only POST method allowed";

        exchange.sendResponseHeaders(
                405,
                response.getBytes().length
        );

        OutputStream os = exchange.getResponseBody();

        os.write(response.getBytes());

        os.close();
    }
});

// ADD EXPENSE API
server.createContext("/add-expense", exchange -> {

    // Only POST method
    if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {

        // Read request body
        InputStreamReader isr = new InputStreamReader(
                exchange.getRequestBody(),
                StandardCharsets.UTF_8
        );

        BufferedReader br = new BufferedReader(isr);

        StringBuilder requestBody = new StringBuilder();

        String line;

        while ((line = br.readLine()) != null) {
            requestBody.append(line);
        }

        // Example:
        // group_id=1&title=Pizza&amount=3000

        String body = requestBody.toString();

        String[] pairs = body.split("&");

        int groupId =
                Integer.parseInt(pairs[0].split("=")[1]);

        String title =
                pairs[1].split("=")[1];

        double amount =
                Double.parseDouble(pairs[2].split("=")[1]);

        try {

            Connection conn = Database.connect();

            String sql =
                    "INSERT INTO expenses(group_id, title, amount) VALUES (?, ?, ?)";

            PreparedStatement stmt =
                    conn.prepareStatement(sql);

            stmt.setInt(1, groupId);

            stmt.setString(2, title);

            stmt.setDouble(3, amount);

            stmt.executeUpdate();

            String response =
                    "Expense added successfully!";

            exchange.sendResponseHeaders(
                    200,
                    response.getBytes().length
            );

            OutputStream os = exchange.getResponseBody();

            os.write(response.getBytes());

            os.close();

            System.out.println("Expense added!");

        } catch (Exception e) {

            String response = "Failed to add expense!";

            exchange.sendResponseHeaders(
                    500,
                    response.getBytes().length
            );

            OutputStream os = exchange.getResponseBody();

            os.write(response.getBytes());

            os.close();

            e.printStackTrace();
        }

    } else {

        String response = "Only POST method allowed";

        exchange.sendResponseHeaders(
                405,
                response.getBytes().length
        );

        OutputStream os = exchange.getResponseBody();

        os.write(response.getBytes());

        os.close();
    }
});

// ADD EXPENSE PAYMENT API
server.createContext("/add-expense-payment", exchange -> {

    // Only POST method
    if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {

        // Read request body
        InputStreamReader isr = new InputStreamReader(
                exchange.getRequestBody(),
                StandardCharsets.UTF_8
        );

        BufferedReader br = new BufferedReader(isr);

        StringBuilder requestBody = new StringBuilder();

        String line;

        while ((line = br.readLine()) != null) {
            requestBody.append(line);
        }

        // Example:
        // expense_id=1&user_id=1&amount_paid=2000

        String body = requestBody.toString();

        String[] pairs = body.split("&");

        int expenseId =
                Integer.parseInt(pairs[0].split("=")[1]);

        int userId =
                Integer.parseInt(pairs[1].split("=")[1]);

        double amountPaid =
                Double.parseDouble(pairs[2].split("=")[1]);

        try {

            Connection conn = Database.connect();

            String sql =
                    "INSERT INTO expense_payments(expense_id, user_id, amount_paid) VALUES (?, ?, ?)";

            PreparedStatement stmt =
                    conn.prepareStatement(sql);

            stmt.setInt(1, expenseId);

            stmt.setInt(2, userId);

            stmt.setDouble(3, amountPaid);

            stmt.executeUpdate();

            String response =
                    "Expense payment added successfully!";

            exchange.sendResponseHeaders(
                    200,
                    response.getBytes().length
            );

            OutputStream os = exchange.getResponseBody();

            os.write(response.getBytes());

            os.close();

            System.out.println("Expense payment added!");

        } catch (Exception e) {

            String response = "Failed to add expense payment!";

            exchange.sendResponseHeaders(
                    500,
                    response.getBytes().length
            );

            OutputStream os = exchange.getResponseBody();

            os.write(response.getBytes());

            os.close();

            e.printStackTrace();
        }

    } else {

        String response = "Only POST method allowed";

        exchange.sendResponseHeaders(
                405,
                response.getBytes().length
        );

        OutputStream os = exchange.getResponseBody();

        os.write(response.getBytes());

        os.close();
    }
});

// ADD EXPENSE PARTICIPANT API
server.createContext("/add-expense-participant", exchange -> {

    // Only POST method
    if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {

        // Read request body
        InputStreamReader isr = new InputStreamReader(
                exchange.getRequestBody(),
                StandardCharsets.UTF_8
        );

        BufferedReader br = new BufferedReader(isr);

        StringBuilder requestBody = new StringBuilder();

        String line;

        while ((line = br.readLine()) != null) {
            requestBody.append(line);
        }

        // Example:
        // expense_id=1&user_id=1

        String body = requestBody.toString();

        String[] pairs = body.split("&");

        int expenseId =
                Integer.parseInt(pairs[0].split("=")[1]);

        int userId =
                Integer.parseInt(pairs[1].split("=")[1]);

        try {

            Connection conn = Database.connect();

            String sql =
                    "INSERT INTO expense_participants(expense_id, user_id) VALUES (?, ?)";

            PreparedStatement stmt =
                    conn.prepareStatement(sql);

            stmt.setInt(1, expenseId);

            stmt.setInt(2, userId);

            stmt.executeUpdate();

            String response =
                    "Expense participant added successfully!";

            exchange.sendResponseHeaders(
                    200,
                    response.getBytes().length
            );

            OutputStream os = exchange.getResponseBody();

            os.write(response.getBytes());

            os.close();

            System.out.println("Expense participant added!");

        } catch (Exception e) {

            String response = "Failed to add expense participant!";

            exchange.sendResponseHeaders(
                    500,
                    response.getBytes().length
            );

            OutputStream os = exchange.getResponseBody();

            os.write(response.getBytes());

            os.close();

            e.printStackTrace();
        }

    } else {

        String response = "Only POST method allowed";

        exchange.sendResponseHeaders(
                405,
                response.getBytes().length
        );

        OutputStream os = exchange.getResponseBody();

        os.write(response.getBytes());

        os.close();
    }
});

        // Start server
        server.start();

        System.out.println("Server started on port 8080");
    }
}