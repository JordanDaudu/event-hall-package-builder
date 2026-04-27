package com.eventhall.controller;

import com.eventhall.dto.QuoteResponse;
import com.eventhall.dto.UpdateQuoteStatusRequest;
import com.eventhall.enums.QuoteStatus;
import com.eventhall.service.QuoteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * A controller is the layer that receives HTTP requests from the frontend.
 *
 * This controller is for admin quote actions:
 * - View all submitted quote requests.
 * - Optionally filter quotes by status.
 * - Update the status of a quote.
 *
 * For now, these endpoints are called "admin" endpoints by URL only.
 * There is no authentication yet, which is okay for the MVP stage.
 */
@RestController
@RequestMapping("/api/admin/quotes")
public class AdminQuoteController {

    /*
     * Controllers should not contain business logic directly.
     * Instead, they call services.
     *
     * QuoteService contains the real quote logic:
     * searching quotes, updating statuses, and converting results to response DTOs.
     */
    private final QuoteService quoteService;

    /*
     * Constructor injection.
     *
     * Spring sees that this controller needs a QuoteService.
     * Since QuoteService is marked with @Service, Spring creates it and passes it here.
     *
     * This is better than creating QuoteService manually with new QuoteService(...)
     * because Spring manages all dependencies for us.
     */
    public AdminQuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    /*
     * Handles:
     * GET /api/admin/quotes
     * GET /api/admin/quotes?status=NEW
     * GET /api/admin/quotes?status=CONTACTED
     *
     * @RequestParam reads a value from the query string.
     * Example: ?status=NEW means status will become QuoteStatus.NEW.
     *
     * required = false means the frontend is allowed to call the endpoint
     * without providing a status filter.
     */
    @GetMapping
    public List<QuoteResponse> getAllQuotes(
            @RequestParam(required = false) QuoteStatus status
    ) {
        /*
         * If the admin supplied a status, return only quotes with that status.
         * Otherwise, return every quote in the database.
         */
        if (status != null) {
            return quoteService.getQuotesByStatus(status);
        }

        return quoteService.getAllQuotes();
    }

    /*
     * Handles:
     * PUT /api/admin/quotes/{id}/status
     *
     * Example:
     * PUT /api/admin/quotes/5/status
     *
     * {id} is a path variable, meaning it is part of the URL itself.
     * The request body contains the new status, for example:
     * {
     *   "status": "CONTACTED"
     * }
     */
    @PutMapping("/{id}/status")
    public QuoteResponse updateQuoteStatus(
            /*
             * @PathVariable takes the {id} value from the URL
             * and puts it into this Long id parameter.
             */
            @PathVariable Long id,

            /*
             * @RequestBody tells Spring to read the JSON body and convert it
             * into an UpdateQuoteStatusRequest object.
             *
             * @Valid tells Spring to check the validation annotations inside
             * UpdateQuoteStatusRequest before calling this method.
             */
            @Valid @RequestBody UpdateQuoteStatusRequest request
    ) {
        return quoteService.updateQuoteStatus(id, request.status());
    }
}