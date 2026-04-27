package com.eventhall.service;

import com.eventhall.dto.EventTypeDto;
import com.eventhall.entity.EventType;
import com.eventhall.repository.EventTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * Service layer for event type logic.
 *
 * A service sits between controllers and repositories.
 * Controllers receive HTTP requests.
 * Repositories talk to the database.
 * Services contain the application logic in the middle.
 */
@Service
public class EventTypeService {

    private final EventTypeRepository eventTypeRepository;

    public EventTypeService(EventTypeRepository eventTypeRepository) {
        this.eventTypeRepository = eventTypeRepository;
    }

    /*
     * Returns all event types as DTOs.
     *
     * The repository returns EventType entities.
     * The service converts them into EventTypeDto objects before sending them
     * back to the controller.
     */
    public List<EventTypeDto> getAllEventTypes() {
        return eventTypeRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    /*
     * Finds one event type by id.
     *
     * findById returns Optional<EventType> because the id might not exist.
     * orElseThrow throws an error if the event type was not found.
     */
    public EventTypeDto getEventTypeById(Long id) {
        EventType eventType = eventTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event type not found with id: " + id));

        return toDto(eventType);
    }

    /*
     * Private helper method to convert an EventType entity into an EventTypeDto.
     */
    private EventTypeDto toDto(EventType eventType) {
        return new EventTypeDto(
                eventType.getId(),
                eventType.getName(),
                eventType.getBasePrice()
        );
    }
}