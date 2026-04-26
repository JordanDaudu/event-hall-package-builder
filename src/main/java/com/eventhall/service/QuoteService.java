package com.eventhall.service;

import com.eventhall.dto.CreateQuoteRequest;
import com.eventhall.dto.EventTypeDto;
import com.eventhall.dto.QuoteResponse;
import com.eventhall.dto.UpgradeDto;
import com.eventhall.entity.*;
import com.eventhall.enums.QuoteStatus;
import com.eventhall.mapper.QuoteMapper;
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
        EventType eventType = eventTypeRepository.findById(request.eventTypeId())
                .orElseThrow(() -> new RuntimeException("Event type not found"));

        List<Upgrade> upgrades = upgradeRepository.findAllById(request.upgradeIds());

        Customer customer = new Customer(
                request.customerName(),
                request.customerEmail()
        );
        customerRepository.save(customer);

        BigDecimal totalPrice = pricingService.calculateTotal(
                toEventTypeDto(eventType),
                request.guestCount(),
                upgrades.stream()
                        .map(this::toUpgradeDto)
                        .toList()
        );

        Quote quote = new Quote(
                customer,
                eventType,
                request.guestCount(),
                totalPrice
        );
        quoteRepository.save(quote);

        for (Upgrade upgrade : upgrades) {
            quoteItemRepository.save(new QuoteItem(quote, upgrade));
        }

        List<QuoteItem> items = quoteItemRepository.findByQuoteId(quote.getId());
        return QuoteMapper.toResponse(quote, items);
    }

    public QuoteResponse getQuoteById(Long id) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quote not found"));

        List<QuoteItem> items = quoteItemRepository.findByQuoteId(id);
        return QuoteMapper.toResponse(quote, items);
    }

    public List<QuoteResponse> getAllQuotes() {
        return quoteRepository.findAll()
                .stream()
                .map(quote -> {
                    List<QuoteItem> items = quoteItemRepository.findByQuoteId(quote.getId());
                    return QuoteMapper.toResponse(quote, items);
                })
                .toList();
    }

    public QuoteResponse updateQuoteStatus(Long id, QuoteStatus status) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quote not found"));

        quote.updateStatus(status);
        quoteRepository.save(quote);

        List<QuoteItem> items = quoteItemRepository.findByQuoteId(id);
        return QuoteMapper.toResponse(quote, items);
    }

    private EventTypeDto toEventTypeDto(EventType eventType) {
        return new EventTypeDto(
                eventType.getId(),
                eventType.getName(),
                eventType.getBasePrice()
        );
    }

    private UpgradeDto toUpgradeDto(Upgrade upgrade) {
        return new UpgradeDto(
                upgrade.getId(),
                upgrade.getName(),
                upgrade.getDescription(),
                upgrade.getCategory(),
                upgrade.getPrice(),
                upgrade.isActive()
        );
    }
}