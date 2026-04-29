package com.eventhall.controller;

import com.eventhall.dto.*;
import com.eventhall.service.CustomerManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin-only REST API for managing customer accounts.
 *
 * All endpoints require the ADMIN role (enforced globally by SecurityConfig
 * for /api/admin/** and also locally via @PreAuthorize for extra clarity).
 *
 * Endpoints:
 *   GET    /api/admin/customers          → list all customers
 *   POST   /api/admin/customers          → create customer
 *   GET    /api/admin/customers/{id}     → get customer detail
 *   PUT    /api/admin/customers/{id}     → update customer profile
 *   PUT    /api/admin/customers/{id}/password → change password
 *   PUT    /api/admin/customers/{id}/active   → enable / disable
 */
@RestController
@RequestMapping("/api/admin/customers")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCustomerController {

    private final CustomerManagementService customerManagementService;

    public AdminCustomerController(CustomerManagementService customerManagementService) {
        this.customerManagementService = customerManagementService;
    }

    @GetMapping
    public List<CustomerResponse> listCustomers() {
        return customerManagementService.listCustomers();
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request
    ) {
        CustomerResponse created = customerManagementService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public CustomerResponse getCustomer(@PathVariable Long id) {
        return customerManagementService.getCustomer(id);
    }

    @PutMapping("/{id}")
    public CustomerResponse updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest request
    ) {
        return customerManagementService.updateCustomer(id, request);
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangeCustomerPasswordRequest request
    ) {
        customerManagementService.changePassword(id, request);
        return ResponseEntity.ok(Map.of("message", "הסיסמה עודכנה בהצלחה"));
    }

    @PutMapping("/{id}/active")
    public CustomerResponse setActive(
            @PathVariable Long id,
            @Valid @RequestBody SetActiveRequest request
    ) {
        return customerManagementService.setActive(id, request.active());
    }
}
