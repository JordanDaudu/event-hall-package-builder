package com.eventhall.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request body for creating a new package option.
 * Used by POST /api/admin/package-options.
 */
public record CreatePackageOptionRequest(

        @NotBlank(message = "שם האפשרות בעברית הוא שדה חובה")
        @Size(max = 120, message = "שם האפשרות בעברית לא יכול לעלות על 120 תווים")
        String nameHe,

        @Size(max = 120, message = "שם האפשרות באנגלית לא יכול לעלות על 120 תווים")
        String nameEn,

        @NotNull(message = "קטגוריה היא שדה חובה")
        PackageOptionCategory category,

        @NotNull(message = "מחיר גלובלי הוא שדה חובה")
        @DecimalMin(value = "0.0", inclusive = true, message = "המחיר חייב להיות אפס או יותר")
        BigDecimal globalPrice,

        Integer sortOrder
) {
    /** Returns the effective sort order, defaulting to 0 if not provided. */
    public int effectiveSortOrder() {
        return sortOrder != null ? sortOrder : 0;
    }
}
