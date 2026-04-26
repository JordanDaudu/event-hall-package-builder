package com.eventhall.controller;

import com.eventhall.dto.EventTypeDto;
import com.eventhall.service.EventTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// @RestController means this class exposes HTTP API endpoints.
// The return values are automatically converted to JSON.
@RestController
@RequestMapping("/api/event-types")
public class EventTypeController {

    private final EventTypeService eventTypeService;

    // Constructor injection:
    // Spring automatically gives us the EventTypeService object.
    public EventTypeController(EventTypeService eventTypeService) {
        this.eventTypeService = eventTypeService;
    }

    // Handles: GET /api/event-types
    @GetMapping
    public List<EventTypeDto> getAllEventTypes() {
        return eventTypeService.getAllEventTypes();
    }
}