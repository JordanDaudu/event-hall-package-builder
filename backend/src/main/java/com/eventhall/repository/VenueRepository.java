package com.eventhall.repository;

import com.eventhall.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {

    List<Venue> findAllByActiveTrueOrderBySortOrderAsc();

    List<Venue> findAllByOrderBySortOrderAscCreatedAtDesc();

    boolean existsByNameHeIgnoreCase(String nameHe);

    boolean existsByNameHeIgnoreCaseAndIdNot(String nameHe, Long excludeId);
}
