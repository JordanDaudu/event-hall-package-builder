package com.eventhall.repository;

import com.eventhall.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 * Repository layer for Customer database operations.
 *
 * Spring Data JPA creates the implementation automatically at runtime.
 * We only define an interface.
 *
 * JpaRepository<Customer, Long> means:
 * - Customer is the entity type managed by this repository.
 * - Long is the type of the Customer primary key id.
 *
 * This gives us built-in methods like:
 * - save(customer)
 * - findById(id)
 * - findAll()
 * - delete(customer)
 * - count()
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}