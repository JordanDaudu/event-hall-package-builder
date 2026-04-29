package com.eventhall.controller;

import com.eventhall.dto.CreateVenueRequest;
import com.eventhall.dto.SetActiveRequest;
import com.eventhall.dto.UpdateVenueRequest;
import com.eventhall.dto.VenueResponse;
import com.eventhall.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin-only venue management endpoints.
 *
 * Endpoints:
 *   GET    /api/admin/venues          → list all venues (active + inactive)
 *   POST   /api/admin/venues          → create venue
 *   GET    /api/admin/venues/{id}     → get single venue
 *   PUT    /api/admin/venues/{id}     → update venue
 *   PUT    /api/admin/venues/{id}/active → toggle active flag
 *   DELETE /api/admin/venues/{id}     → soft-delete (sets active=false)
 */
@RestController
@RequestMapping("/api/admin/venues")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVenueController {

    private final VenueService venueService;

    public AdminVenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @GetMapping
    public List<VenueResponse> listAll() {
        return venueService.listAll();
    }

    @PostMapping
    public ResponseEntity<VenueResponse> create(
            @Valid @RequestBody CreateVenueRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(venueService.create(request));
    }

    @GetMapping("/{id}")
    public VenueResponse getById(@PathVariable Long id) {
        return venueService.getById(id);
    }

    @PutMapping("/{id}")
    public VenueResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVenueRequest request
    ) {
        return venueService.update(id, request);
    }

    @PutMapping("/{id}/active")
    public VenueResponse setActive(
            @PathVariable Long id,
            @Valid @RequestBody SetActiveRequest request
    ) {
        return venueService.setActive(id, request.active());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        venueService.delete(id);
        return ResponseEntity.ok(Map.of("message", "האולם הוסר בהצלחה"));
    }
}
