package com.eventhall.dto;

import jakarta.validation.constraints.*;

public record UpdateVenueRequest(

        @NotBlank(message = "שם האולם בעברית הוא שדה חובה")
        @Size(max = 200, message = "שם האולם לא יכול לעלות על 200 תווים")
        String nameHe,

        @Size(max = 200, message = "שם האולם באנגלית לא יכול לעלות על 200 תווים")
        String nameEn,

        String descriptionHe,

        @Size(max = 500, message = "כתובת תמונה לא יכולה לעלות על 500 תווים")
        String imageUrl,

        @NotNull(message = "סדר תצוגה הוא שדה חובה")
        @Min(value = 0, message = "סדר תצוגה לא יכול להיות שלילי")
        Integer sortOrder
) {}
