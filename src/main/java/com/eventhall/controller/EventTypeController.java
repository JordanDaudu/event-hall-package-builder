package com.eventhall.controller;

import com.eventhall.dto.EventTypeDto;
import com.eventhall.service.EventTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
 * Controller for public event type endpoints.
 *
 * Event types are the base package categories customers can choose from,
 * such as Wedding, Birthday, Corporate Event, or Bar/Bat Mitzvah.
 */
@RestController
@RequestMapping("/api/event-types")
public class EventTypeController {

    /*
     * The controller depends on the service, not directly on the repository.
     *
     * This keeps the controller focused on HTTP input/output.
     * The service handles application logic and data conversion.
     */
    private final EventTypeService eventTypeService;

    public EventTypeController(EventTypeService eventTypeService) {
        this.eventTypeService = eventTypeService;
    }

    /*
     * Handles:
     * GET /api/event-types
     *
     * @GetMapping means this method responds to HTTP GET requests.
     *
     * Returning List<EventTypeDto> means Spring will automatically convert
     * the Java list into a JSON array for the frontend.
     */
    @GetMapping
    public List<EventTypeDto> getAllEventTypes() {
        return eventTypeService.getAllEventTypes();
    }
}