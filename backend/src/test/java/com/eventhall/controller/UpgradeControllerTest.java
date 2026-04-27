package com.eventhall.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UpgradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllUpgrades_shouldReturnSeededActiveUpgrades() throws Exception {
        mockMvc.perform(get("/api/upgrades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItems(
                        "Flowers",
                        "DJ",
                        "Lighting",
                        "Live Plants",
                        "Stage Design"
                )));
    }
}