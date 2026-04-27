package com.eventhall.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

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
}