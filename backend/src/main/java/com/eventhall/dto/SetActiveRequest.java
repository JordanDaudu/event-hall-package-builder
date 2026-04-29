package com.eventhall.dto;

import jakarta.validation.constraints.NotNull;

public record SetActiveRequest(

        @NotNull(message = "שדה active הוא חובה")
        Boolean active
) {}
