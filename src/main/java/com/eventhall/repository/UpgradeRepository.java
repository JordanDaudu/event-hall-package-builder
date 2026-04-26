package com.eventhall.repository;

import com.eventhall.entity.Upgrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UpgradeRepository extends JpaRepository<Upgrade, Long> {

    List<Upgrade> findByActiveTrue();
}