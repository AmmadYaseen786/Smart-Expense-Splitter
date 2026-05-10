CREATE DATABASE expense_splitter;

USE expense_splitter;

-- USERS TABLE
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    password VARCHAR(100)
);

-- GROUPS TABLE
CREATE TABLE groups_table (
    id INT PRIMARY KEY AUTO_INCREMENT,
    group_name VARCHAR(100),
    created_by INT,

    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- GROUP MEMBERS
CREATE TABLE group_members (
    id INT PRIMARY KEY AUTO_INCREMENT,
    group_id INT,
    user_id INT,

    FOREIGN KEY (group_id) REFERENCES groups_table(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- EXPENSES TABLE
CREATE TABLE expenses (
    id INT PRIMARY KEY AUTO_INCREMENT,
    group_id INT,
    title VARCHAR(100),
    amount DOUBLE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (group_id) REFERENCES groups_table(id)
);

-- EXPENSE PAYMENTS TABLE
CREATE TABLE expense_payments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    expense_id INT,
    user_id INT,
    amount_paid DOUBLE,

    FOREIGN KEY (expense_id) REFERENCES expenses(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- EXPENSE PARTICIPANTS TABLE
CREATE TABLE expense_participants (
    id INT PRIMARY KEY AUTO_INCREMENT,
    expense_id INT,
    user_id INT,

    FOREIGN KEY (expense_id) REFERENCES expenses(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- PERSONAL EXPENSES TABLE
CREATE TABLE personal_expenses (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    title VARCHAR(100),
    amount DOUBLE,
    category VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id)
);