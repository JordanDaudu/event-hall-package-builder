package com.eventhall.dto;

/**
 * Broad category for grouping package options in the builder UI.
 * Stored as a STRING in the DB so that renaming or reordering
 * in the future doesn't require a column-value migration.
 */
public enum PackageOptionCategory {
    CATERING,
    DECORATION,
    MUSIC,
    PHOTOGRAPHY,
    EXTRAS,
    CHUPPAH,
    AISLE,
    TABLE_FRAME,
    TABLE_FLOWER,
    TABLE_CANDLE,
    NAPKIN,
    TABLECLOTH,
    BRIDE_CHAIR
}
