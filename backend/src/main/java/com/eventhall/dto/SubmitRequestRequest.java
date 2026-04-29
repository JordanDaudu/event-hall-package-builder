package com.eventhall.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Request body for POST /api/customer/requests — submitting a new package request.
 *
 * The customer id is never accepted from this body; it is always extracted
 * from the authenticated JWT principal.
 */
public record SubmitRequestRequest(

        @NotNull(message = "יש לבחור אולם")
        Long venueId,

        @NotEmpty(message = "יש לבחור לפחות אפשרות חבילה אחת")
        List<@NotNull Long> optionIds,

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
        LocalDate eventDate,

        @Min(value = 0, message = "מספר שולחנות הפרשים חייב להיות 0 לפחות")
        @Max(value = 100, message = "מספר שולחנות הפרשים גבוה מדי")
        Integer knightTableCount
) {
}
