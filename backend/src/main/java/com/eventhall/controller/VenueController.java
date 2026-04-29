package com.eventhall.controller;

import com.eventhall.dto.VenueResponse;
import com.eventhall.service.VenueService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public/customer-facing venue endpoint.
 *
 * GET /api/venues — returns only active venues ordered by sortOrder.
 *
 * Accessible to both authenticated customers and admins (and optionally
 * unauthenticated users if the frontend needs to show venues before login).
 * The endpoint contains no sensitive data so broad access is intentional.
 */
@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @GetMapping
    public List<VenueResponse> listActiveVenues() {
        return venueService.listActive();
    }
}
