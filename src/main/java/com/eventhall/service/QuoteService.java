package com.eventhall.service;

import com.eventhall.dto.CreateQuoteRequest;
import com.eventhall.dto.QuoteResponse;
import com.eventhall.entity.*;
import com.eventhall.enums.QuoteStatus;
import com.eventhall.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class QuoteService {

    private final EventTypeRepository eventTypeRepository;
    private final UpgradeRepository upgradeRepository;
    private final CustomerRepository customerRepository;
    private final QuoteRepository quoteRepository;
    private final QuoteItemRepository quoteItemRepository;
    private final PricingService pricingService;

    public QuoteService(
            EventTypeRepository eventTypeRepository,
            UpgradeRepository upgradeRepository,
            CustomerRepository customerRepository,
            QuoteRepository quoteRepository,
            QuoteItemRepository quoteItemRepository,
            PricingService pricingService
    ) {
        this.eventTypeRepository = eventTypeRepository;
        this.upgradeRepository = upgradeRepository;
        this.customerRepository = customerRepository;
        this.quoteRepository = quoteRepository;
        this.quoteItemRepository = quoteItemRepository;
        this.pricingService = pricingService;
    }

    public QuoteResponse createQuote(CreateQuoteRequest request) {

        // 1. Fetch event type
        EventType eventType = eventTypeRepository.findById(request.eventTypeId())
                .orElseThrow(() -> new RuntimeException("Event type not found"));

        // 2. Fetch upgrades
        List<Upgrade> upgrades = upgradeRepository.findAllById(request.upgradeIds());

        // 3. Create customer
        Customer customer = new Customer(
                request.customerName(),
                request.customerEmail()
        );
        customerRepository.save(customer);

        // 4. Calculate price
        BigDecimal totalPrice = pricingService.calculateTotal(
                new com.eventhall.dto.EventTypeDto(eventType.getId(), eventType.getName(), eventType.getBasePrice()),
                request.guestCount(),
                upgrades.stream().map(u ->
                        new com.eventhall.dto.UpgradeDto(
                                u.getId(),
                                u.getName(),
                                u.getDescription(),
                                u.getCategory(),
                                u.getPrice(),
                                u.isActive()
                        )
                ).toList()
        );

        // 5. Create quote
        Quote quote = new Quote(
                customer,
                eventType,
                request.guestCount(),
                totalPrice
        );

        quoteRepository.save(quote);

        // 6. Create quote items
        for (Upgrade upgrade : upgrades) {
            QuoteItem item = new QuoteItem(quote, upgrade);
            quoteItemRepository.save(item);
        }

        // 7. Return response
        return new QuoteResponse(
                quote.getId(),
                eventType.getName(),
                quote.getGuestCount(),
                upgrades.stream().map(Upgrade::getName).toList(),
                quote.getTotalPrice(),
                QuoteStatus.NEW
        );
    }

    public QuoteResponse getQuoteById(Long id) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quote not found"));

        List<QuoteItem> items = quoteItemRepository.findByQuoteId(id);

        return new QuoteResponse(
                quote.getId(),
                quote.getEventType().getName(),
                quote.getGuestCount(),
                items.stream().map(i -> i.getUpgrade().getName()).toList(),
                quote.getTotalPrice(),
                quote.getStatus()
        );
    }

    public List<QuoteResponse> getAllQuotes() {
        return quoteRepository.findAll()
                .stream()
                .map(q -> {
                    List<QuoteItem> items = quoteItemRepository.findByQuoteId(q.getId());

                    return new QuoteResponse(
                            q.getId(),
                            q.getEventType().getName(),
                            q.getGuestCount(),
                            items.stream().map(i -> i.getUpgrade().getName()).toList(),
                            q.getTotalPrice(),
                            q.getStatus()
                    );
                })
                .toList();
    }

    public QuoteResponse updateQuoteStatus(Long id, QuoteStatus status) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quote not found"));

        quote.updateStatus(status);
        quoteRepository.save(quote);

        List<QuoteItem> items = quoteItemRepository.findByQuoteId(id);

        return new QuoteResponse(
                quote.getId(),
                quote.getEventType().getName(),
                quote.getGuestCount(),
                items.stream().map(i -> i.getUpgrade().getName()).toList(),
                quote.getTotalPrice(),
                quote.getStatus()
        );
    }
}