package com.eventhall.controller;

import com.eventhall.dto.QuoteResponse;
import com.eventhall.service.QuoteService;
import com.eventhall.dto.UpdateQuoteStatusRequest;
import com.eventhall.enums.QuoteStatus;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/quotes")
public class AdminQuoteController {

    private final QuoteService quoteService;

    public AdminQuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    // Handles: GET /api/admin/quotes
    @GetMapping
    public List<QuoteResponse> getAllQuotes() {
        return quoteService.getAllQuotes();
    }

    @PutMapping("/{id}/status")
    public QuoteResponse updateQuoteStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateQuoteStatusRequest request
    ) {
        return quoteService.updateQuoteStatus(id, request.status());
    }
}