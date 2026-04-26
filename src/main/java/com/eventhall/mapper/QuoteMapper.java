package com.eventhall.mapper;

import com.eventhall.dto.QuoteResponse;
import com.eventhall.entity.Quote;
import com.eventhall.entity.QuoteItem;
import com.eventhall.entity.Upgrade;

import java.util.List;

public class QuoteMapper {

    public static QuoteResponse toResponse(Quote quote, List<QuoteItem> items) {
        return new QuoteResponse(
                quote.getId(),
                quote.getEventType().getName(),
                quote.getGuestCount(),
                items.stream()
                        .map(QuoteItem::getUpgrade)
                        .map(Upgrade::getName)
                        .toList(),
                quote.getTotalPrice(),
                quote.getStatus()
        );
    }
}