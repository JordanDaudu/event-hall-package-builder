package com.eventhall.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Request body for POST /api/customer/requests — submitting a new package request.
 *
 * Table selections are split into regularTableDesign (always required) and
 * knightTableDesign (required only when knightTableCount > 0), so the backend
 * can validate and snapshot each table type independently.
 *
 * The customer id is never accepted from this body; it is always extracted
 * from the authenticated JWT principal.
 */
public record SubmitRequestRequest(

        @NotNull(message = "יש לבחור אולם")
        Long venueId,

        @NotNull(message = "יש לבחור חופה")
        Long chuppahOptionId,

        List<Long> chuppahUpgradeIds,

        Long aisleOptionId,

        /** Regular table design — always required. */
        @NotNull(message = "יש להזין עיצוב שולחן רגיל")
        @Valid
        TableDesignRequest regularTableDesign,

        @Min(value = 0, message = "מספר שולחנות האבירים חייב להיות 0 לפחות")
        @Max(value = 4, message = "מספר שולחנות האבירים לא יכול לעלות על 4")
        Integer knightTableCount,

        /** Knight table design — required only when knightTableCount > 0. */
        @Valid
        TableDesignRequest knightTableDesign,

        Long napkinOptionId,

        Long tableclothOptionId,

        Long brideChairOptionId,

        @NotBlank(message = "מספר תעודת זהות הוא שדה חובה")
        @Size(max = 50, message = "מספר תעודת זהות ארוך מדי")
        String eventCustomerIdentityNumber,

        @NotBlank(message = "שם איש הקשר הוא שדה חובה")
        @Size(max = 200, message = "שם ארוך מדי")
        String eventContactName,

        @NotBlank(message = "מספר טלפון הוא שדה חובה")
        @Size(max = 50, message = "מספר טלפון ארוך מדי")
        String eventContactPhoneNumber,

        @NotNull(message = "תאריך האירוע הוא שדה חובה")
        @FutureOrPresent(message = "תאריך האירוע לא יכול להיות בעבר")
        LocalDate eventDate
) {
    /** Safe accessor — returns an empty list if chuppahUpgradeIds is null. */
    public List<Long> safeUpgradeIds() {
        return chuppahUpgradeIds != null ? chuppahUpgradeIds : List.of();
    }

    /** Returns the effective knight table count, defaulting to 0 if null. */
    public int safeKnightCount() {
        return knightTableCount != null ? knightTableCount : 0;
    }
}
