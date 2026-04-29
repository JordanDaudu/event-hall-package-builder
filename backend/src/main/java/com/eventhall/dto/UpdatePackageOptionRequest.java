package com.eventhall.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request body for updating an existing package option.
 * All fields are optional — only non-null values are applied (PATCH semantics).
 * Used by PUT /api/admin/package-options/{id}.
 */
public record UpdatePackageOptionRequest(

        @Size(max = 120, message = "שם האפשרות בעברית לא יכול לעלות על 120 תווים")
        String nameHe,

        @Size(max = 120, message = "שם האפשרות באנגלית לא יכול לעלות על 120 תווים")
        String nameEn,

        PackageOptionCategory category,

        @DecimalMin(value = "0.0", inclusive = true, message = "המחיר חייב להיות אפס או יותר")
        BigDecimal globalPrice,

        Integer sortOrder
) {}
