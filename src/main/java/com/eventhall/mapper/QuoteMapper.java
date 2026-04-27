package com.eventhall.mapper;

import com.eventhall.dto.QuoteResponse;
import com.eventhall.entity.Quote;
import com.eventhall.entity.QuoteItem;
import com.eventhall.entity.Upgrade;

import java.util.List;

/*
 * Mapper class for converting Quote entities into QuoteResponse DTOs.
 *
 * A mapper helps keep conversion logic out of controllers and services.
 * The entity is the database model.
 * The DTO is the API model.
 * This class translates between them.
 */
public class QuoteMapper {

    /*
     * Static method because this mapper currently has no dependencies and no state.
     *
     * It receives:
     * - quote: the main quote entity
     * - items: the selected upgrades connected to that quote
     *
     * It returns a clean response for the frontend.
     */
    public static QuoteResponse toResponse(Quote quote, List<QuoteItem> items) {
        return new QuoteResponse(
                quote.getId(),
                quote.getCustomer().getName(),
                quote.getCustomer().getEmail(),
                quote.getCustomer().getPhoneNumber(),
                quote.getEventType().getName(),
                quote.getGuestCount(),

                /*
                 * Convert QuoteItem objects into upgrade names.
                 *
                 * items.stream() starts a stream over the quote items.
                 * map(QuoteItem::getUpgrade) turns each QuoteItem into its Upgrade.
                 * map(Upgrade::getName) turns each Upgrade into its name.
                 * toList() collects the names into a List<String>.
                 */
                items.stream()
                        .map(QuoteItem::getUpgrade)
                        .map(Upgrade::getName)
                        .toList(),

                quote.getTotalPrice(),
                quote.getStatus()
        );
    }
}