package com.eventhall.service;

import com.eventhall.dto.EventTypeDto;
import com.eventhall.entity.EventType;
import com.eventhall.repository.EventTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventTypeService {

    private final EventTypeRepository eventTypeRepository;

    public EventTypeService(EventTypeRepository eventTypeRepository) {
        this.eventTypeRepository = eventTypeRepository;
    }

    public List<EventTypeDto> getAllEventTypes() {
        return eventTypeRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public EventTypeDto getEventTypeById(Long id) {
        EventType eventType = eventTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event type not found with id: " + id));

        return toDto(eventType);
    }

    private EventTypeDto toDto(EventType eventType) {
        return new EventTypeDto(
                eventType.getId(),
                eventType.getName(),
                eventType.getBasePrice()
        );
    }
}