# 01 — Project Setup

## Overview

In this step, we created the foundation for the Event Hall Package Builder backend using Spring Boot.

The goal of this stage is to:

- Initialize a clean backend project
- Add required dependencies
- Prepare a scalable project structure

---

## Technologies Used

- Java 21
- Spring Boot
- Maven
- Spring Web
- Spring Validation
- Later: Spring Data JPA
- Later: PostgreSQL
- Later: Swagger UI / OpenAPI

---

## Creating the Project

The project was created using Spring Boot with Maven.

### Required Dependencies

Spring Web:

- Used to build REST APIs
- Enables annotations such as `@RestController`, `@GetMapping`, and `@PostMapping`

Validation:

- Enables request validation using annotations such as:
    - `@NotNull`
    - `@NotBlank`
    - `@Email`

---

## Project Structure

We follow a layered architecture to keep the code clean and maintainable.

    src/main/java/com/eventhall
    ├── controller
    ├── service
    ├── repository
    ├── entity
    ├── dto
    ├── enums
    ├── exception
    └── EventHallApplication.java

### Why this structure?

Each layer has a clear responsibility.

Controller:

- Handles HTTP requests and responses

Service:

- Contains business logic

Repository:

- Handles database access later with JPA

Entity:

- Represents database tables later

DTO:

- Used for transferring data between client and server

Enums:

- Stores fixed values like quote status

Exception:

- Contains custom error handling later

---

## Key Design Decision

### Layered Architecture

We separate responsibilities into layers instead of putting everything in one class.

### Why?

- Improves readability
- Makes code easier to maintain
- Allows independent testing of components
- Follows industry best practices

---

## Application Entry Point

The application starts from:

    @SpringBootApplication
    public class EventHallApplication {
        public static void main(String[] args) {
            SpringApplication.run(EventHallApplication.class, args);
        }
    }

### What does `@SpringBootApplication` do?

It combines three important Spring annotations:

- `@Configuration` — marks this as a configuration class
- `@EnableAutoConfiguration` — lets Spring Boot automatically configure the application
- `@ComponentScan` — scans the project for Spring components like controllers and services

---

## Result

At the end of this step:

- The Spring Boot project is running successfully
- The base structure is ready
- We are prepared to start building REST APIs

---

## Next Step

We will start implementing our first endpoints:

- `GET /api/event-types`
- `GET /api/upgrades`

These will initially return in-memory fake data before connecting to a database.