package com.eventhall.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateCustomerRequest(

        @NotBlank(message = "שם מלא הוא שדה חובה")
        @Size(max = 200, message = "שם מלא לא יכול לעלות על 200 תווים")
        String fullName,

        @NotBlank(message = "כתובת דוא\"ל היא שדה חובה")
        @Email(message = "כתובת דוא\"ל אינה תקינה")
        @Size(max = 200, message = "כתובת דוא\"ל לא יכולה לעלות על 200 תווים")
        String email,

        @Size(max = 50, message = "מספר תעודת זהות לא יכול לעלות על 50 תווים")
        String customerIdentityNumber,

        @Size(max = 50, message = "מספר טלפון לא יכול לעלות על 50 תווים")
        String phoneNumber,

        @NotBlank(message = "סיסמה היא שדה חובה")
        @Size(min = 8, message = "הסיסמה חייבת להכיל לפחות 8 תווים")
        String password,

        @NotNull(message = "מחיר בסיס הוא שדה חובה")
        @DecimalMin(value = "0", message = "מחיר בסיס לא יכול להיות שלילי")
        BigDecimal basePackagePrice
) {}
