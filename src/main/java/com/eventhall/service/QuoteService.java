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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * Service layer for quote operations.
 *
 * This is one of the most important classes in the backend because it coordinates
 * several parts of the system:
 * - EventTypeRepository
 * - UpgradeRepository
 * - CustomerRepository
 * - QuoteRepository
 * - QuoteItemRepository
 * - PricingService
 *
 * The controller stays simple and calls this service.
 */
@Service
public class QuoteService {

    private final EventTypeRepository eventTypeRepository;
    private final UpgradeRepository upgradeRepository;
    private final CustomerRepository customerRepository;
    private final QuoteRepository quoteRepository;
    private final QuoteItemRepository quoteItemRepository;
    private final PricingService pricingService;

    /*
     * Constructor injection for all dependencies.
     *
     * Spring automatically provides these repository and service objects.
     */
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

    /*
     * Creates a new quote from the customer request.
     *
     * Full flow:
     * 1. Find the selected event type.
     * 2. Find the selected upgrades.
     * 3. Create and save the customer.
     * 4. Calculate the final price in the backend.
     * 5. Create and save the quote.
     * 6. Create quote items linking the quote to each upgrade.
     * 7. Return a clean QuoteResponse DTO.
     */
    public QuoteResponse createQuote(CreateQuoteRequest request) {
        /*
         * Find the event type chosen by the customer.
         * If the id does not exist, throw an error handled by GlobalExceptionHandler.
         */
        EventType eventType = eventTypeRepository.findById(request.eventTypeId())
                .orElseThrow(() -> new RuntimeException("Event type not found"));

        /*
         * Find all selected upgrades by id.
         *
         * request.upgradeIds() might be [1, 2, 5].
         * findAllById returns matching Upgrade entities from the database.
         *
         * This makes sure:
         * - Every requested upgrade exists.
         * - No inactive upgrade is selected.
         * - No duplicate upgrade IDs are sent.
         */
        List<Upgrade> upgrades = getValidSelectedUpgrades(request.upgradeIds());

        /*
         * Create a new customer entity from the request.
         * For the MVP, every submitted quote creates a new customer row.
         * Later, with authentication, customers might have accounts instead.
         */
        Customer customer = new Customer(
                request.customerName(),
                request.customerEmail(),
                request.customerPhoneNumber()
        );
        customerRepository.save(customer);

        /*
         * Calculate the total price in the backend.
         *
         * PricingService currently works with DTOs, so we convert entities to DTOs.
         * This keeps the pricing logic independent from JPA entities.
         */
        BigDecimal totalPrice = pricingService.calculateTotal(
                toEventTypeDto(eventType),
                request.guestCount(),
                upgrades.stream()
                        .map(this::toUpgradeDto)
                        .toList()
        );

        /*
         * Create the main Quote entity.
         * The Quote constructor automatically sets:
         * - status = NEW
         * - createdAt = now
         */
        Quote quote = new Quote(
                customer,
                eventType,
                request.guestCount(),
                totalPrice
        );
        quoteRepository.save(quote);

        /*
         * Save one QuoteItem row for each selected upgrade.
         *
         * This connects the quote to its selected upgrades in the database.
         */
        for (Upgrade upgrade : upgrades) {
            quoteItemRepository.save(new QuoteItem(quote, upgrade));
        }

        /*
         * Read the saved quote items back from the database and convert everything
         * into a response DTO for the frontend.
         */
        List<QuoteItem> items = quoteItemRepository.findByQuoteId(quote.getId());
        return QuoteMapper.toResponse(quote, items);
    }

    /*
     * Gets one quote by id for the public quote summary page.
     */
    public QuoteResponse getQuoteById(Long id) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quote not found"));

        List<QuoteItem> items = quoteItemRepository.findByQuoteId(id);
        return QuoteMapper.toResponse(quote, items);
    }

    /*
     * Gets all quotes that match a specific status.
     * Used by the admin status filter.
     */
    public List<QuoteResponse> getQuotesByStatus(QuoteStatus status) {
        return quoteRepository.findByStatus(status)
                .stream()
                .map(q -> {
                    List<QuoteItem> items = quoteItemRepository.findByQuoteId(q.getId());
                    return QuoteMapper.toResponse(q, items);
                })
                .toList();
    }

    /*
     * Gets every quote in the database for the admin quote list.
     */
    public List<QuoteResponse> getAllQuotes() {
        return quoteRepository.findAll()
                .stream()
                .map(quote -> {
                    List<QuoteItem> items = quoteItemRepository.findByQuoteId(quote.getId());
                    return QuoteMapper.toResponse(quote, items);
                })
                .toList();
    }

    @Transactional
    public QuoteResponse updateQuoteStatus(Long id, QuoteStatus status) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quote not found"));

        quote.updateStatus(status);
        quoteRepository.save(quote);

        List<QuoteItem> items = quoteItemRepository.findByQuoteId(id);
        return QuoteMapper.toResponse(quote, items);
    }

    /*
     * This method validates the upgrade IDs selected by the customer.
     *
     * We keep it private because it is only used inside QuoteService.
     */
    private List<Upgrade> getValidSelectedUpgrades(List<Long> upgradeIds) {
        /*
         * Empty list is valid.
         * It means the customer selected no upgrades.
         */
        if (upgradeIds.isEmpty()) {
            return List.of();
        }

        /*
         * Reject duplicate upgrade IDs.
         *
         * Example invalid request:
         * "upgradeIds": [1, 1, 2]
         *
         * Without this check, the same upgrade could be charged twice
         * and saved twice as a QuoteItem.
         */
        Set<Long> uniqueUpgradeIds = new HashSet<>(upgradeIds);

        if (uniqueUpgradeIds.size() != upgradeIds.size()) {
            throw new RuntimeException("Duplicate upgrade IDs are not allowed");
        }

        /*
         * Load all upgrades matching the requested IDs.
         *
         * Important:
         * findAllById does not throw an error if some IDs do not exist.
         * That is why we compare the amount requested with the amount found.
         */
        List<Upgrade> upgrades = upgradeRepository.findAllById(upgradeIds);

        if (upgrades.size() != upgradeIds.size()) {
            Set<Long> foundIds = upgrades.stream()
                    .map(Upgrade::getId)
                    .collect(java.util.stream.Collectors.toSet());

            List<Long> missingIds = upgradeIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();

            throw new RuntimeException("Upgrade not found with id(s): " + missingIds);
        }

        /*
         * Soft-deleted upgrades still exist in the database,
         * but active = false.
         *
         * Customers should not be able to select inactive upgrades.
         */
        List<Long> inactiveIds = upgrades.stream()
                .filter(upgrade -> !upgrade.isActive())
                .map(Upgrade::getId)
                .toList();

        if (!inactiveIds.isEmpty()) {
            throw new RuntimeException("Inactive upgrades cannot be selected. Invalid id(s): " + inactiveIds);
        }

        return upgrades;
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