package com.eventhall.entity;

import jakarta.persistence.*;

/*
 * QuoteItem connects a Quote to an Upgrade.
 *
 * Why do we need this table?
 * A quote can have many upgrades, and the same upgrade can appear in many quotes.
 * That is a many-to-many relationship.
 *
 * Instead of using @ManyToMany directly, this project uses an explicit join entity.
 * That is a professional choice because later QuoteItem can store extra data such as:
 * - priceAtTimeOfQuote
 * - quantity
 * - notes
 */
@Entity
public class QuoteItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Many quote items can belong to one quote.
     * In the database this usually creates a quote_id foreign key column.
     */
    @ManyToOne
    private Quote quote;

    /*
     * Many quote items can reference one upgrade.
     * In the database this usually creates an upgrade_id foreign key column.
     */
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