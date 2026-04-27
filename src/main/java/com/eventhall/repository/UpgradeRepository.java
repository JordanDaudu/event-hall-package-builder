package com.eventhall.repository;

import com.eventhall.entity.Upgrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/*
 * Repository for Upgrade entities.
 */
public interface UpgradeRepository extends JpaRepository<Upgrade, Long> {

    /*
     * Spring Data JPA reads this method name and builds a query automatically.
     *
     * findByActiveTrue means:
     * "Find all upgrades where active = true."
     *
     * This supports the public upgrades endpoint because customers should not
     * see inactive or soft-deleted upgrades.
     */
    List<Upgrade> findByActiveTrue();
}