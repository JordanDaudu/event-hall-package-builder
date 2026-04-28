package com.eventhall.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "business")
public record BusinessConfig(
        String name,
        String contactEmail,
        String contactPhone
) {}