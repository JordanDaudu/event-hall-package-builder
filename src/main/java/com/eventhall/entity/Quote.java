package com.eventhall.entity;

import com.eventhall.enums.QuoteStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int guestCount;

    private BigDecimal totalPrice;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private QuoteStatus status;

    // Many quotes can belong to one customer.
    @ManyToOne
    private Customer customer;

    // Many quotes can use one event type.
    @ManyToOne
    private EventType eventType;

    protected Quote() {
    }

    public Quote(Customer customer, EventType eventType, int guestCount, BigDecimal totalPrice) {
        this.customer = customer;
        this.eventType = eventType;
        this.guestCount = guestCount;
        this.totalPrice = totalPrice;
        this.status = QuoteStatus.NEW;
        this.createdAt = LocalDateTime.now();
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

    public QuoteStatus getStatus() {
        return status;
    }

    public Customer getCustomer() {
        return customer;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void updateStatus(QuoteStatus status) {
        this.status = status;
    }
}