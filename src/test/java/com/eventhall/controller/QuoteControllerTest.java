package com.eventhall.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QuoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createQuote_shouldReturnCreatedQuoteWithCalculatedPrice() throws Exception {
        String requestBody = """
                {
                  "eventTypeId": 1,
                  "guestCount": 100,
                  "upgradeIds": [1, 2],
                  "customerName": "Jordan",
                  "customerEmail": "jordan@example.com",
                  "customerPhoneNumber": "0501234567"
                }
                """;

        mockMvc.perform(post("/api/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventTypeName").value("Wedding"))
                .andExpect(jsonPath("$.guestCount").value(100))
                .andExpect(jsonPath("$.upgrades", hasItems("Flowers", "DJ")))
                .andExpect(jsonPath("$.totalPrice").value(18000.00))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    void createQuote_withInvalidRequest_shouldReturnBadRequest() throws Exception {
        String requestBody = """
            {
              "eventTypeId": null,
              "guestCount": 0,
              "upgradeIds": null,
              "customerName": "",
              "customerEmail": "not-an-email",
              "customerPhoneNumber": ""
            }
            """;

        mockMvc.perform(post("/api/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.eventTypeId").value("Event type is required"))
                .andExpect(jsonPath("$.guestCount").value("Guest count must be at least 1"))
                .andExpect(jsonPath("$.upgradeIds").value("Upgrade IDs list is required"))
                .andExpect(jsonPath("$.customerName").value("Customer name is required"))
                .andExpect(jsonPath("$.customerEmail").value("Customer email must be valid"))
                .andExpect(jsonPath("$.customerPhoneNumber").value("Customer phone number is required"));
    }

    @Test
    void createQuote_withInvalidPhoneNumber_shouldReturnBadRequest() throws Exception {
        String requestBody = """
        {
          "eventTypeId": 1,
          "guestCount": 100,
          "upgradeIds": [1, 2],
          "customerName": "Jordan",
          "customerEmail": "jordan@example.com",
          "customerPhoneNumber": "abc"
        }
        """;

        mockMvc.perform(post("/api/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.customerPhoneNumber").value("Customer phone number must be valid"));
    }

    @Test
    void getQuoteById_shouldReturnCreatedQuote() throws Exception {
        String requestBody = """
            {
              "eventTypeId": 1,
              "guestCount": 50,
              "upgradeIds": [1],
              "customerName": "Jordan",
              "customerEmail": "jordan@example.com",
              "customerPhoneNumber": "0501234567"
            }
            """;

        String response = mockMvc.perform(post("/api/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number quoteId = com.jayway.jsonpath.JsonPath.read(response, "$.id");

        mockMvc.perform(get("/api/quotes/" + quoteId.longValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(quoteId.intValue()))
                .andExpect(jsonPath("$.eventTypeName").value("Wedding"))
                .andExpect(jsonPath("$.guestCount").value(50))
                .andExpect(jsonPath("$.upgrades", hasItems("Flowers")))
                .andExpect(jsonPath("$.totalPrice").value(8500.00))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    void createQuote_withUnknownUpgradeId_shouldReturnBadRequest() throws Exception {
        String requestBody = """
            {
              "eventTypeId": 1,
              "guestCount": 100,
              "upgradeIds": [1, 999],
              "customerName": "Jordan",
              "customerEmail": "jordan@example.com",
              "customerPhoneNumber": "0501234567"
            }
            """;

        mockMvc.perform(post("/api/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Upgrade not found with id(s): [999]"));
    }

    @Test
    void createQuote_withDuplicateUpgradeIds_shouldReturnBadRequest() throws Exception {
        String requestBody = """
            {
              "eventTypeId": 1,
              "guestCount": 100,
              "upgradeIds": [1, 1],
              "customerName": "Jordan",
              "customerEmail": "jordan@example.com",
              "customerPhoneNumber": "0501234567"
            }
            """;

        mockMvc.perform(post("/api/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Duplicate upgrade IDs are not allowed"));
    }

    @Test
    void createQuote_withInactiveUpgrade_shouldReturnBadRequest() throws Exception {
        String createUpgradeResponse = mockMvc.perform(post("/api/admin/upgrades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "Inactive Test Upgrade",
                              "description": "This upgrade will be deactivated",
                              "category": "Test",
                              "price": 1000
                            }
                            """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number upgradeId = com.jayway.jsonpath.JsonPath.read(createUpgradeResponse, "$.id");

        mockMvc.perform(delete("/api/admin/upgrades/" + upgradeId.longValue()))
                .andExpect(status().isOk());

        String requestBody = """
            {
              "eventTypeId": 1,
              "guestCount": 100,
              "upgradeIds": [%d],
              "customerName": "Jordan",
              "customerEmail": "jordan@example.com",
              "customerPhoneNumber": "0501234567"
            }
            """.formatted(upgradeId.longValue());

        mockMvc.perform(post("/api/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "Inactive upgrades cannot be selected. Invalid id(s): [" + upgradeId.longValue() + "]"
                ));
    }
}