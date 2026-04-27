package com.eventhall.repository;

import com.eventhall.entity.EventType;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 * Repository for EventType entities.
 *
 * Because it extends JpaRepository, Spring automatically gives us common
 * CRUD database methods without writing SQL manually.
 */
public interface EventTypeRepository extends JpaRepository<EventType, Long> {
}