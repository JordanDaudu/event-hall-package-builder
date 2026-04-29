package com.eventhall.dto;

import java.util.List;

/**
 * Represents the design selections for one table type (regular or knight).
 * Used as a nested object within {@link SubmitRequestRequest}.
 */
public record TableDesignRequest(

        /** Required: the frame option chosen for this table. Must be TABLE_FRAME category. */
        Long frameOptionId,

        /** Required: the primary flower option chosen. Must be TABLE_FLOWER category. */
        Long primaryFlowerOptionId,

        /**
         * Optional: a secondary small flower option.
         * Only allowed when the primary flower has flowerSize=LARGE.
         * Must be TABLE_FLOWER category with flowerSize=SMALL.
         */
        Long secondarySmallFlowerOptionId,

        /**
         * Candle selection mode: "RANDOM" or "SELECTED".
         * RANDOM = the hall decides; no candleHolderOptionIds required.
         * SELECTED = customer chooses 1–3 candle holder options.
         * Defaults to RANDOM if null.
         */
        String candleSelectionMode,

        /**
         * Selected candle holder option IDs.
         * Must be empty when candleSelectionMode=RANDOM.
         * Must contain 1–3 IDs when candleSelectionMode=SELECTED.
         * Must all be TABLE_CANDLE category.
         */
        List<Long> candleHolderOptionIds
) {
    /** Safe accessor — returns an empty list if candleHolderOptionIds is null. */
    public List<Long> safeCandleHolderIds() {
        return candleHolderOptionIds != null ? candleHolderOptionIds : List.of();
    }

    /** Returns the effective candle selection mode, defaulting to RANDOM if null. */
    public String effectiveCandleMode() {
        return candleSelectionMode != null ? candleSelectionMode.toUpperCase() : "RANDOM";
    }
}
