package com.eventhall.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminUpgradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createUpdateDeleteUpgrade_flow_shouldWorkCorrectly() throws Exception {

        // 1. CREATE
        String createResponse = mockMvc.perform(post("/api/admin/upgrades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Test Upgrade",
                                  "description": "Test description",
                                  "category": "Test",
                                  "price": 5000
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Upgrade"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number upgradeId = JsonPath.read(createResponse, "$.id");

        // 2. UPDATE
        mockMvc.perform(put("/api/admin/upgrades/" + upgradeId.longValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated Upgrade",
                                  "description": "Updated description",
                                  "category": "Updated",
                                  "price": 6000,
                                  "active": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Upgrade"));

        // 3. DELETE (soft delete)
        mockMvc.perform(delete("/api/admin/upgrades/" + upgradeId.longValue()))
                .andExpect(status().isOk());

        // 4. VERIFY NOT IN PUBLIC LIST
        mockMvc.perform(get("/api/upgrades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", not(hasItems("Updated Upgrade"))));
    }

    @Test
    void getAllUpgradesForAdmin_shouldReturnActiveAndInactiveUpgrades() throws Exception {
        // Soft delete an existing seeded upgrade.
        mockMvc.perform(delete("/api/admin/upgrades/4"))
                .andExpect(status().isOk());

        // Public endpoint should hide inactive upgrade.
        mockMvc.perform(get("/api/upgrades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", not(hasItems("Live Plants"))));

        // Admin endpoint should still show inactive upgrade.
        mockMvc.perform(get("/api/admin/upgrades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItems("Live Plants")))
                .andExpect(jsonPath("$[?(@.name == 'Live Plants')].active", hasItems(false)));
    }
}