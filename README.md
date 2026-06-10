# Upgraded Smart Expense Splitter

An upgraded, production-style, full-stack web application for splitting shared expenses. The application has been redesigned from a single-payer model to support multi-contributor bill logging, Spring Security authentication, group rosters, private personal cost trackers, and dynamic Splitwise-style debt resolution plans.

---

## Redesigned Architecture

The project has been separated into independent modules:
- **`backend/`**: A Spring Boot REST application.
- **`frontend/`**: A clean static web client.
Spring Boot is configured to host the frontend assets seamlessly from the root-level `frontend/` directory.

---

## Features

1. **Authentication**: Register and log in. Includes Spring Security session mapping and password hashing (BCrypt).
2. **Groups**: Manage multiple groups and link users as members.
3. **Multi-Contributor Splits**: Record expenses paid by multiple members in different proportions, and split the bill among custom participants.
4. **Personal Trackers**: Manage private personal expenses that are hidden from group members.
5. **Real-time Balance Calculations**: Calculates group net balances using:
   $$\text{User Balance} = \text{Total Contributions} - \text{Total Share}$$
6. **Greedy Debt Resolution Guide**: Recalculates splits to advise "who owes how much to whom" inside any group.
7. **Clean Dashboard**: Renders personal totals, group shares, receivables, outstandings, and recent activities.

---

## OOP Principles Demonstrated

- **Encapsulation**: Models use private fields accessible only via Lombok getters/setters.
- **Inheritance**: Models inherit `id`, `createdAt`, and `updatedAt` from [BaseEntity](backend/src/main/java/com/expensesplitter/model/BaseEntity.java).
- **Abstraction**: Services are defined as interfaces (e.g. `UserService`) to separate REST routing from DB operations.
- **Polymorphism**: Services are implemented dynamically in the `impl/` subpackage.

---

## Database Setup

1. Make sure your MySQL Server is running on port `3306`.
2. Create the database:
   ```sql
   CREATE DATABASE expense_splitter;
   ```
3. Update database credentials in `backend/src/main/resources/application.properties` if needed.
4. Hibernate will automatically build the tables on startup.

---

## Pre-seeded Demo Data
On startup, a CommandLineRunner seeds a default user so you can log in immediately:
- **Email**: `ammad@gmail.com`
- **Password**: `ammad123`
- **Name**: `Ammad`

---

## API Endpoints

### 1. Authentication (`/api/auth`)
- `POST /register` : Create user
- `POST /login` : Programmatic session authentication
- `POST /logout` : Invalidate user session
- `GET /me` : Fetch logged-in profile

### 2. Group Management (`/api/groups`)
- `POST /` : Create group
- `GET /` : List groups user belongs to
- `GET /{id}` : View group details
- `POST /{id}/members` : Add member by email

### 3. Shared Expenses (`/api/expenses`)
- `POST /` : Create expense bill (multi-contributor list & participants checklist)
- `GET ?groupId={id}` : List expenses in a group

### 4. Personal Expenses (`/api/personal-expenses`)
- `POST /` : Create private expense
- `GET /` : Fetch owner's private log
- `DELETE /{id}` : Delete private expense

### 5. Calculations (`/api/balances`)
- `GET /groups/{groupId}` : Returns group split balance sheet
- `GET /dashboard` : Compiles dashboard statistics

---

## Running Instructions

### Run in IntelliJ IDEA
1. Open IntelliJ.
2. Select **File > Open** and open the `backend` folder.
3. IntelliJ will load it as a Maven module.
4. Run [ExpenseSplitterApplication.java](backend/src/main/java/com/expensesplitter/ExpenseSplitterApplication.java) located under `backend/src/main/java/com/expensesplitter/`.
5. The application will start on [http://localhost:8080](http://localhost:8080).

### Run from Command Line
In the `backend` folder:
- **Build**: `./mvnw clean compile`
- **Run**: `./mvnw spring-boot:run`

### Accessing the Web Client
Once the backend is running, open your browser and navigate to:
- [http://localhost:8080/index.html](http://localhost:8080/index.html) (or simply [http://localhost:8080](http://localhost:8080)).
- Alternatively, you can double-click `frontend/index.html` directly from your file explorer.

### REST API Testing
Import `smart-expense-splitter.postman_collection.json` located at the root of the project into Postman.
- First call the **Login (Ammad)** request. Postman will automatically capture and persist the session cookie, allowing all subsequent authenticated requests to execute successfully.
