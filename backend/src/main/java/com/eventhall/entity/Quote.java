package com.eventhall.entity;

import com.eventhall.enums.QuoteStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * Entity for quote requests.
 *
 * A Quote is created when a customer submits the package builder form.
 * It stores:
 * - The selected event type.
 * - The customer.
 * - The guest count.
 * - The final backend-calculated price.
 * - The quote status.
 * - The creation time.
 */
@Entity
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int guestCount;

    private BigDecimal totalPrice;

    private LocalDateTime createdAt;

    private LocalDateTime approvedAt;

    /*
     * Enums can be stored in the database in different ways.
     *
     * EnumType.STRING stores the enum name, such as "NEW" or "APPROVED".
     * This is safer and more readable than storing numbers like 0, 1, 2.
     */
    @Enumerated(EnumType.STRING)
    private QuoteStatus status;

    /*
     * Many quotes can belong to one customer.
     *
     * Example:
     * One customer may submit several quote requests over time.
     * In the database, this usually creates a customer_id foreign key column
     * in the quote table.
     */
    @ManyToOne
    private Customer customer;

    /*
     * Many quotes can use one event type.
     *
     * Example:
     * Many different customers can choose the Wedding event type.
     * In the database, this usually creates an event_type_id foreign key column
     * in the quote table.
     */
    @ManyToOne
    private EventType eventType;

    /*
     * One quote can have many quote items.
     *
     * Each QuoteItem represents one selected upgrade inside this quote.
     * mappedBy = "quote" means the QuoteItem entity owns the relationship
     * through its quote field.
     */
    @OneToMany(mappedBy = "quote")
    private List<QuoteItem> quoteItems = new ArrayList<>();

    /*
     * Required by JPA for reading quote rows from the database.
     */
    protected Quote() {
    }

    /*
     * Constructor used by the service when creating a new quote.
     *
     * Notice that status and createdAt are set by the backend,
     * not by the frontend. This is important because the frontend should not
     * decide whether a new quote is approved or what time it was created.
     */
    public Quote(Customer customer, EventType eventType, int guestCount, BigDecimal totalPrice) {
        this.customer = customer;
        this.eventType = eventType;
        this.guestCount = guestCount;
        this.totalPrice = totalPrice;
        this.status = QuoteStatus.NEW;
        this.createdAt = LocalDateTime.now();
        this.approvedAt = null;
    }

    public Long getId() {
        return id;
    }

    public int getGuestCount() {
        return guestCount;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public QuoteStatus getStatus() {
        return status;
    }

    public Customer getCustomer() {
        return customer;
    }

    public EventType getEventType() {
        return eventType;
    }

    public List<QuoteItem> getQuoteItems() {
        return quoteItems;
    }

    /*
     * Domain method for changing the quote status.
     *
     * Instead of allowing outside code to directly change the field,
     * we expose a clear method that says what behavior is happening.
     */
    public void updateStatus(QuoteStatus status) {
        this.status = status;

        if (status == QuoteStatus.APPROVED) {
            this.approvedAt = LocalDateTime.now();
        } else {
            this.approvedAt = null;
        }
    }
}