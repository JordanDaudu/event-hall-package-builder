package com.eventhall.service;

import com.eventhall.dto.CreateVenueRequest;
import com.eventhall.dto.UpdateVenueRequest;
import com.eventhall.dto.VenueResponse;
import com.eventhall.entity.Venue;
import com.eventhall.repository.VenueRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Business logic for venue management.
 *
 * Soft-delete convention: "deleting" a venue sets active=false so that
 * historical package requests that reference the venue remain consistent.
 * The admin list shows all venues; the customer/public list shows only active ones.
 */
@Service
@Transactional
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    // -----------------------------------------------------------------------
    // Read
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<VenueResponse> listActive() {
        return venueRepository.findAllByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(VenueResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VenueResponse> listAll() {
        return venueRepository.findAllByOrderBySortOrderAscCreatedAtDesc()
                .stream()
                .map(VenueResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public VenueResponse getById(Long id) {
        return VenueResponse.from(requireVenue(id));
    }

    // -----------------------------------------------------------------------
    // Create
    // -----------------------------------------------------------------------

    public VenueResponse create(CreateVenueRequest req) {
        if (venueRepository.existsByNameHeIgnoreCase(req.nameHe())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "שם אולם זה כבר קיים במערכת");
        }

        Venue venue = Venue.builder()
                .nameHe(req.nameHe())
                .nameEn(req.nameEn())
                .descriptionHe(req.descriptionHe())
                .imageUrl(req.imageUrl())
                .active(true)
                .sortOrder(req.sortOrder() != null ? req.sortOrder() : 0)
                .build();

        return VenueResponse.from(venueRepository.save(venue));
    }

    // -----------------------------------------------------------------------
    // Update
    // -----------------------------------------------------------------------

    public VenueResponse update(Long id, UpdateVenueRequest req) {
        Venue venue = requireVenue(id);

        if (venueRepository.existsByNameHeIgnoreCaseAndIdNot(req.nameHe(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "שם אולם זה כבר קיים במערכת");
        }

        venue.setNameHe(req.nameHe());
        venue.setNameEn(req.nameEn());
        venue.setDescriptionHe(req.descriptionHe());
        venue.setImageUrl(req.imageUrl());
        venue.setSortOrder(req.sortOrder() != null ? req.sortOrder() : venue.getSortOrder());

        return VenueResponse.from(venueRepository.save(venue));
    }

    // -----------------------------------------------------------------------
    // Active toggle (enable / disable)
    // -----------------------------------------------------------------------

    public VenueResponse setActive(Long id, boolean active) {
        Venue venue = requireVenue(id);
        venue.setActive(active);
        return VenueResponse.from(venueRepository.save(venue));
    }

    // -----------------------------------------------------------------------
    // Soft-delete
    // -----------------------------------------------------------------------

    /**
     * Soft-deletes a venue by setting active=false.
     * A hard-delete is not performed because future package requests will
     * reference venues and must remain consistent.
     */
    public void delete(Long id) {
        Venue venue = requireVenue(id);
        venue.setActive(false);
        venueRepository.save(venue);
    }

    // -----------------------------------------------------------------------
    // Package-request helper (used by Phase 7 — package request submission)
    // -----------------------------------------------------------------------

    /**
     * Validates that the given venue id exists and is active.
     * Throws 404 or 400 so the package request controller can reject invalid venue ids.
     */
    @Transactional(readOnly = true)
    public Venue requireActiveVenue(Long venueId) {
        Venue venue = requireVenue(venueId);
        if (!venue.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "האולם שנבחר אינו זמין כרגע");
        }
        return venue;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Venue requireVenue(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "אולם לא נמצא"));
    }
}
