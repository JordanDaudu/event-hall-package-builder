package com.eventhall.repository;

import com.eventhall.entity.QuoteItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/*
 * Repository for QuoteItem entities.
 *
 * QuoteItem connects quotes to upgrades.
 */
public interface QuoteItemRepository extends JpaRepository<QuoteItem, Long> {

    /*
     * Spring Data JPA query method.
     *
     * By writing findByQuoteId, Spring understands that we want to find all
     * QuoteItem rows where the related Quote has the given id.
     *
     * We do not write SQL here. Spring creates the query based on the method name.
     */
    List<QuoteItem> findByQuoteId(Long quoteId);
}