package com.eventhall.dto;

import com.eventhall.entity.PackageRequestItem;

import java.math.BigDecimal;

/**
 * Represents a single line item within a PackageRequest detail response.
 */
public record PackageRequestItemResponse(
        Long id,
        Long packageOptionId,
        String optionNameSnapshot,
        BigDecimal globalPriceSnapshot,
        BigDecimal customerOverridePriceSnapshot,
        BigDecimal finalPrice,
        boolean hasCustomerOverride,
        PackageOptionCategory category,
        /** REGULAR | KNIGHT for table items; null for non-table items. */
        String tableContext
) {
    public static PackageRequestItemResponse from(PackageRequestItem item) {
        return new PackageRequestItemResponse(
                item.getId(),
                item.getPackageOption().getId(),
                item.getOptionNameSnapshot(),
                item.getGlobalPriceSnapshot(),
                item.getCustomerOverridePriceSnapshot(),
                item.getFinalPrice(),
                item.getCustomerOverridePriceSnapshot() != null,
                item.getPackageOption().getCategory(),
                item.getTableContext()
        );
    }
}
