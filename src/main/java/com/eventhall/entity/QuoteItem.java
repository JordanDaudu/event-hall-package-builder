package com.eventhall.entity;

import jakarta.persistence.*;

@Entity
public class QuoteItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Quote quote;

    @ManyToOne
    private Upgrade upgrade;

    protected QuoteItem() {
    }

    public QuoteItem(Quote quote, Upgrade upgrade) {
        this.quote = quote;
        this.upgrade = upgrade;
    }

    public Long getId() {
        return id;
    }

    public Quote getQuote() {
        return quote;
    }

    public Upgrade getUpgrade() {
        return upgrade;
    }
}