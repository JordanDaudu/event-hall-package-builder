package com.eventhall.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminQuoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllQuotes_shouldReturnSubmittedQuotes() throws Exception {
        createQuote();

        mockMvc.perform(get("/api/admin/quotes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].eventTypeName", hasItems("Wedding")))
                .andExpect(jsonPath("$[*].status", hasItems("NEW")));
    }

    @Test
    void updateQuoteStatus_shouldChangeStatus() throws Exception {
        Number quoteId = createQuote();

        mockMvc.perform(put("/api/admin/quotes/" + quoteId.longValue() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "CONTACTED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(quoteId.intValue()))
                .andExpect(jsonPath("$.status").value("CONTACTED"));
    }

    @Test
    void updateQuoteStatus_withInvalidStatus_shouldReturnBadRequest() throws Exception {
        Number quoteId = createQuote();

        mockMvc.perform(put("/api/admin/quotes/" + quoteId.longValue() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "DONE"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "Invalid request body. Check that all fields use the correct format and enum values."
                ));
    }

    @Test
    void getAllQuotes_withStatusFilter_shouldReturnOnlyMatchingQuotes() throws Exception {
        Number quoteId = createQuote();

        mockMvc.perform(put("/api/admin/quotes/" + quoteId.longValue() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "status": "CONTACTED"
                            }
                            """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/quotes?status=CONTACTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItems(quoteId.intValue())))
                .andExpect(jsonPath("$[*].status", hasItems("CONTACTED")));

        mockMvc.perform(get("/api/admin/quotes?status=NEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", not(hasItems(quoteId.intValue()))));
    }

    private Number createQuote() throws Exception {
        String response = mockMvc.perform(post("/api/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventTypeId": 1,
                                  "guestCount": 100,
                                  "upgradeIds": [1, 2],
                                  "customerName": "Jordan",
                                  "customerEmail": "jordan@example.com",
                                  "customerPhoneNumber": "0501234567"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(response, "$.id");
    }
}