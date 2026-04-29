package com.eventhall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeCustomerPasswordRequest(

        @NotBlank(message = "סיסמה היא שדה חובה")
        @Size(min = 8, message = "הסיסמה חייבת להכיל לפחות 8 תווים")
        String newPassword
) {}
