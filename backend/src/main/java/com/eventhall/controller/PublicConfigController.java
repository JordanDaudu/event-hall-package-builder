package com.eventhall.controller;

import com.eventhall.config.BusinessConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicConfigController {

    private final BusinessConfig businessConfig;

    public PublicConfigController(BusinessConfig businessConfig) {
        this.businessConfig = businessConfig;
    }

    @GetMapping("/api/config")
    public BusinessConfig getConfig() {
        return businessConfig;
    }
}