package com.eventhall.repository;

import com.eventhall.entity.PackageRequest;
import com.eventhall.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PackageRequestRepository extends JpaRepository<PackageRequest, Long> {

    List<PackageRequest> findByCustomer_IdOrderBySubmittedAtDesc(Long customerId);

    List<PackageRequest> findAllByStatusOrderBySubmittedAtDesc(RequestStatus status);

    List<PackageRequest> findAllByOrderBySubmittedAtDesc();

    @Query("SELECT r FROM PackageRequest r WHERE r.customer.id = :customerId AND r.id = :id")
    Optional<PackageRequest> findByIdAndCustomerId(@Param("id") Long id, @Param("customerId") Long customerId);
}
