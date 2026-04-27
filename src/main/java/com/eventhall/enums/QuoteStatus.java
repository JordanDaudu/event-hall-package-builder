package com.eventhall.enums;

/*
 * Enum = a fixed set of allowed values.
 *
 * QuoteStatus defines the lifecycle of a quote request.
 * Using an enum prevents random invalid strings from being used in Java code.
 */
public enum QuoteStatus {
    /*
     * A quote was submitted by a customer and has not been handled yet.
     */
    NEW,

    /*
     * An admin or business owner contacted the customer.
     */
    CONTACTED,

    /*
     * The quote was accepted or approved.
     */
    APPROVED,

    /*
     * The quote was rejected or did not move forward.
     */
    REJECTED
}