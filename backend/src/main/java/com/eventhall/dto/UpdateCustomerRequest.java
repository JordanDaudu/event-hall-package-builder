package com.eventhall.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UpdateCustomerRequest(

        @NotBlank(message = "שם מלא הוא שדה חובה")
        @Size(max = 200, message = "שם מלא לא יכול לעלות על 200 תווים")
        String fullName,

        @Size(max = 50, message = "מספר תעודת זהות לא יכול לעלות על 50 תווים")
        String customerIdentityNumber,

        @Size(max = 50, message = "מספר טלפון לא יכול לעלות על 50 תווים")
        String phoneNumber,

        @NotNull(message = "מחיר בסיס הוא שדה חובה")
        @DecimalMin(value = "0", message = "מחיר בסיס לא יכול להיות שלילי")
        BigDecimal basePackagePrice
) {}
