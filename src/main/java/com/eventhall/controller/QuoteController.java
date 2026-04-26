package com.eventhall.controller;

import com.eventhall.dto.CreateQuoteRequest;
import com.eventhall.dto.QuoteResponse;
import com.eventhall.service.QuoteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    // Handles: POST /api/quotes
    // @RequestBody tells Spring to read JSON from the request body
    // and convert it into a CreateQuoteRequest object.
    @PostMapping
    public QuoteResponse createQuote(@Valid @RequestBody CreateQuoteRequest request) {
        return quoteService.createQuote(request);
    }

    // Handles: GET /api/quotes/{id}
    @GetMapping("/{id}")
    public QuoteResponse getQuoteById(@PathVariable Long id) {
        return quoteService.getQuoteById(id);
    }
}