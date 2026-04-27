package com.eventhall.controller;

import com.eventhall.dto.CreateQuoteRequest;
import com.eventhall.dto.QuoteResponse;
import com.eventhall.service.QuoteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/*
 * Public quote controller.
 *
 * This is used by the customer side of the application.
 * A customer can:
 * - Submit a quote request.
 * - Get a quote by id after it was created.
 */
@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    /*
     * QuoteService contains the actual quote creation logic:
     * - Find selected event type.
     * - Find selected upgrades.
     * - Create customer.
     * - Calculate backend price.
     * - Save quote and quote items.
     */
    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    /*
     * Handles:
     * POST /api/quotes
     *
     * The frontend sends JSON like:
     * {
     *   "eventTypeId": 1,
     *   "guestCount": 100,
     *   "upgradeIds": [1, 2],
     *   "customerName": "Jordan",
     *   "customerEmail": "jordan@example.com"
     *   "customerPhoneNumber": "0501234567"
     * }
     *
     * @RequestBody converts that JSON into a CreateQuoteRequest record.
     * @Valid runs validation before the service is called.
     */
    @PostMapping
    public QuoteResponse createQuote(@Valid @RequestBody CreateQuoteRequest request) {
        return quoteService.createQuote(request);
    }

    /*
     * Handles:
     * GET /api/quotes/{id}
     *
     * This is useful after submitting a quote.
     * The frontend can request the saved quote summary using its id.
     */
    @GetMapping("/{id}")
    public QuoteResponse getQuoteById(@PathVariable Long id) {
        return quoteService.getQuoteById(id);
    }
}