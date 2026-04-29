package com.eventhall.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

/*
 * GlobalExceptionHandler centralizes error handling for the whole API.
 *
 * Without this class, validation errors and bad JSON errors would return
 * default Spring error responses that are harder for beginners and frontends to read.
 *
 * @RestControllerAdvice means:
 * - This class can handle exceptions thrown by any @RestController.
 * - Returned objects are automatically converted to JSON.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /*
     * Handles validation errors triggered by @Valid.
     *
     * Example:
     * If CreateQuoteRequest has customerEmail = "not-an-email",
     * Spring throws MethodArgumentNotValidException before entering the controller method.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(
            MethodArgumentNotValidException ex
    ) {
        Map<String, String> errors = new HashMap<>();

        /*
         * getFieldErrors() gives us all invalid fields.
         * For each error, we place:
         * - key = field name
         * - value = validation message
         *
         * Example JSON response:
         * {
         *   "customerEmail": "Customer email must be valid",
         *   "guestCount": "Guest count must be at least 1"
         * }
         */
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        return errors;
    }

    /*
     * Handles generic runtime errors.
     *
     * For the MVP, services throw RuntimeException when something is not found.
     * Example: "Quote not found" or "Upgrade not found with id: 5".
     *
     * Later, you can replace this with custom exceptions like NotFoundException
     * and return more specific HTTP statuses such as 404 Not Found.
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleRuntimeException(RuntimeException ex) {
        return Map.of("error", ex.getMessage());
    }

    /*
     * Handles invalid JSON or values that cannot be converted.
     *
     * Example:
     * If the frontend sends:
     * { "status": "DONE" }
     *
     * DONE is not a valid QuoteStatus enum value, so Spring cannot convert it.
     * This handler returns a cleaner error message.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInvalidJson(HttpMessageNotReadableException ex) {
        return Map.of(
                "error",
                "Invalid request body. Check that all fields use the correct format and enum values."
        );
    }

    /*
     * Handles ResponseStatusException (used by AuthService and other
     * services that explicitly raise an HTTP status, e.g. 401/403/404).
     * We respect the chosen status code and return a JSON {error: "..."} body.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException ex) {
        String message = ex.getReason() != null ? ex.getReason() : ex.getMessage();
        return ResponseEntity.status(ex.getStatusCode())
                .body(Map.of("error", message));
    }

    /*
     * Catch-all for unexpected non-runtime exceptions (and other Throwables
     * not handled above). We log the full stack server-side and return a
     * generic 500 to clients so internal details don't leak.
     *
     * RuntimeException above is intentionally separate to preserve legacy
     * service-layer behavior; it will be tightened/removed as the legacy
     * code is replaced in later phases.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error"));
    }
}