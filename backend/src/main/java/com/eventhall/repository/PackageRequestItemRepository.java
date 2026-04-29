package com.eventhall.repository;

import com.eventhall.entity.PackageRequestItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PackageRequestItemRepository extends JpaRepository<PackageRequestItem, Long> {

    List<PackageRequestItem> findByPackageRequest_IdOrderById(Long requestId);
}
