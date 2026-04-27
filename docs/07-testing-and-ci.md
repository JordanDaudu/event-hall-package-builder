# 07 — Testing and Continuous Integration (CI)

## Overview

In this stage, we introduced automated testing and continuous integration (CI) to ensure the backend is stable, reliable, and production-ready.

This includes:

- Unit testing for business logic
- Integration testing for API endpoints
- Using an H2 in-memory database for tests
- Creating a separate test profile
- Running tests automatically using GitHub Actions

---

## 1. Basic Spring Context Test

We added a simple test to verify that the Spring application context loads correctly.

Example:

    @SpringBootTest
    class EventHallApplicationTests {

        @Test
        void contextLoads() {
        }

    }

### Purpose

- Ensures that the application starts without errors
- Detects configuration issues early
- Serves as a baseline test

---

## 2. H2 Test Database

We introduced H2 as an in-memory database for testing.

### Why H2?

- Fast startup
- No external dependencies
- Automatically resets between test runs
- Ideal for CI environments

---

## 3. Test Profile

We created a dedicated test profile using:

    @ActiveProfiles("test")

### Purpose

- Separates test configuration from development configuration
- Uses H2 instead of PostgreSQL during tests
- Prevents tests from affecting real data

### Example configuration

    spring.datasource.url=jdbc:h2:mem:testdb
    spring.datasource.driverClassName=org.h2.Driver
    spring.jpa.hibernate.ddl-auto=create-drop

---

## 4. GitHub Actions (CI)

We added a CI pipeline using GitHub Actions.

### Behavior

On every:

- Push
- Pull Request

GitHub Actions will:

1. Build the project
2. Run all tests:

       mvn test

---

## 5. CI Issue and Fix

### Problem

CI initially failed because PostgreSQL was not available in the GitHub environment.

### Solution

- Introduced H2 test database
- Used:

      @ActiveProfiles("test")

- Fully decoupled tests from local PostgreSQL

---

## 6. Service-Level Testing

We added a unit test for the pricing logic.

Example:

    @Test
    void calculateTotal_shouldAddBasePriceTimesGuestCountAndUpgradePrices() {

        EventTypeDto eventType = new EventTypeDto(1L, "Wedding", BigDecimal.valueOf(120));

        List<UpgradeDto> upgrades = List.of(
            new UpgradeDto(1L, "Flowers", "...", "Decor", BigDecimal.valueOf(2500), true),
            new UpgradeDto(2L, "DJ", "...", "Entertainment", BigDecimal.valueOf(3500), true)
        );

        BigDecimal result = pricingService.calculateTotal(eventType, 100, upgrades);

        assertEquals(BigDecimal.valueOf(18000), result);
    }

### Purpose

- Tests business logic in isolation
- Fast and reliable (no database required)

---

## 7. API Integration Testing (MockMvc)

We added full API tests using MockMvc.

### What is MockMvc?

MockMvc simulates HTTP requests without starting a real server.

Example:

    mockMvc.perform(post("/api/quotes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalPrice").value(18000.00));

---

## 8. Tested Endpoints

### Public API

- GET /api/event-types
- GET /api/upgrades
- POST /api/quotes
- GET /api/quotes/{id}

### Admin API

- GET /api/admin/quotes
- PUT /api/admin/quotes/{id}/status
- GET /api/admin/quotes?status=STATUS

### Admin Upgrade API

- POST /api/admin/upgrades
- PUT /api/admin/upgrades/{id}
- DELETE /api/admin/upgrades/{id}

---

## 9. Validation Testing

We added tests for invalid input handling.

Example:

    mockMvc.perform(post("/api/quotes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidRequest))
        .andExpect(status().isBadRequest());

### Purpose

- Ensures backend validation works correctly
- Prevents invalid data from entering the system

---

## 10. Filtering Tests

We added tests for quote filtering.

Endpoint:

    GET /api/admin/quotes?status=CONTACTED

### Behavior

- Returns only quotes matching the requested status
- Ensures filtering logic works correctly

---

## 11. Admin Upgrade Testing

We added full CRUD tests for upgrades.

### Flow tested

1. Create upgrade
2. Update upgrade
3. Soft delete upgrade
4. Verify it is hidden from the public API

### Key concept

Soft delete:

    upgrade.active = false;

Only active upgrades are returned to users.

---

## 12. JSON Type Handling Issue

During testing, we encountered:

    ClassCastException: Integer cannot be cast to Long

### Cause

JSON numbers are parsed as Integer by default.

### Fix

    Number id = JsonPath.read(response, "$.id");

---

## 13. Final Result

At the end of this stage:

- 12 automated tests
- Unit + integration coverage
- Full API flow tested
- Validation and error handling verified
- Admin and public endpoints covered
- CI runs tests on every push

---

## Summary

The backend now has a strong testing foundation with automated validation of both business logic and API behavior.

This level of testing is suitable for a production-style portfolio project.