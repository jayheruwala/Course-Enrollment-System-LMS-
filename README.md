# Learning Management System (LMS)

A robust, enterprise-grade Learning Management System backend built with **Spring Boot 3** and **Java 21**. This platform allows Instructors to create and manage courses, assignments, and quizzes, while Students can enroll, complete lessons, and track their progress. It is fully secured using **Spring Security** and **JWT (JSON Web Tokens)**.

## 🚀 Features

- **Role-Based Access Control (RBAC):** Distinct roles for `STUDENT`, `INSTRUCTOR`, and `ADMIN`.
- **Course Management:** Instructors can create, update, and categorize courses.
- **Enrollment System:** Strict validations for course capacity and student enrollment limits (max 5 active courses).
- **Interactive Learning:** Support for structured Lessons, Quizzes with automated grading, and Assignments with manual grading workflows.
- **Analytics:** Instructors can view course completion rates and student progress.
- **Security First:** Stateless JWT authentication, secure password hashing (BCrypt), and rigorous ownership validations (e.g., instructors can only grade submissions for courses they own).
- **Interactive Documentation:** Automatically generated OpenAPI 3.0 specification via Swagger.

## 🛠️ Technology Stack

- **Core:** Java 21, Spring Boot 3.3.0
- **Database:** PostgreSQL, Spring Data JPA / Hibernate
- **Security:** Spring Security, JWT (jjwt)
- **Validation:** Spring Boot Starter Validation
- **Documentation:** Springdoc OpenAPI (Swagger)
- **Build Tool:** Maven Wrapper

## 📋 Prerequisites

Before running the application, ensure you have the following installed:
- **Java Development Kit (JDK) 21**
- **PostgreSQL** (running locally or via Docker)

> **Important IDE Note:** If you are running this in an IDE like IntelliJ IDEA, ensure your *Project SDK* and *Java Compiler bytecode version* are strictly set to **Java 21**. Newer/Early Access compilers (like Java 26) will break the Lombok annotation processor!

## ⚙️ Setup and Installation

1. **Clone the repository** (if applicable) and navigate to the root directory.
   ```bash
   cd LMS
   ```

2. **Configure the Database**
   Ensure PostgreSQL is running and create a database (the application defaults to `lms` or whatever is configured in your `application.properties`).

   Update the database credentials in `src/main/resources/application.properties` if necessary:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/lms
   spring.datasource.username=your_db_username
   spring.datasource.password=your_db_password
   ```

3. **Build and Run**
   You can run the application directly using the Maven wrapper:
   ```bash
   ./mvnw clean compile spring-boot:run
   ```
   The server will start on `http://localhost:8080`.

## 📖 API Documentation (Swagger)

The project includes an interactive Swagger UI for testing the API endpoints directly from your browser.

Once the server is running, navigate to:
- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON Spec:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **OpenAPI YAML Spec:** [http://localhost:8080/v3/api-docs.yaml](http://localhost:8080/v3/api-docs.yaml)

### Authentication Flow (For API Testing)
1. Register a new user via `POST /api/auth/register` (specifying a `role` of `STUDENT` or `INSTRUCTOR`).
2. Login via `POST /api/auth/login` to receive your JWT access token.
3. In the Swagger UI, click the **Authorize** button at the top right and paste your token to unlock secured endpoints.

## 🗂️ Core Endpoints Overview

- **Authentication:** `/api/auth/register`, `/api/auth/login`, `/api/auth/refresh`
- **Courses:** `/api/courses` (CRUD, Search, Completion Rates, Enrolled Students)
- **Enrollments:** `/api/enrollments` (Enroll, Drop, My Enrollments, Progress)
- **Lessons:** `/api/lessons` (Create, Update, Delete)
- **Quizzes:** `/api/quizzes` (Create, Update, Attempt, Score Calculation)
- **Assignments:** `/api/assignments` (Create, Submit, Grade)

---
*Built with ❤️ using Spring Boot.*
# Course-Enrollment-System-LMS-
