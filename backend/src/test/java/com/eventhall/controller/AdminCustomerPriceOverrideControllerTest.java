package com.eventhall.controller;

import com.eventhall.dto.PackageOptionCategory;
import com.eventhall.entity.PackageOption;
import com.eventhall.repository.PackageOptionRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for GET/POST/DELETE /api/admin/customers/{customerId}/price-overrides.
 *
 * Uses a fresh Spring context with H2 in-memory database (@DirtiesContext).
 * The admin account is seeded by UserAccountSeeder at startup.
 * PackageOption fixtures are inserted directly via repository for test isolation
 * (no admin CRUD for package options exists until Phase 6).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AdminCustomerPriceOverrideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PackageOptionRepository packageOptionRepository;

    private String adminToken;
    private Long customerId;
    private Long adminUserId;
    private Long optionId;

    @BeforeEach
    void setUp() throws Exception {
        adminToken = loginAsAdmin();
        customerId = createTestCustomer();
        adminUserId = fetchAdminUserId();
        optionId = createTestPackageOption();
    }

    // -----------------------------------------------------------------------
    // GET — list overrides
    // -----------------------------------------------------------------------

    @Test
    void listOverrides_forNewCustomer_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/admin/customers/" + customerId + "/price-overrides")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void listOverrides_forUnknownCustomerId_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/admin/customers/999999/price-overrides")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void listOverrides_forNonCustomerUser_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/admin/customers/" + adminUserId + "/price-overrides")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    // -----------------------------------------------------------------------
    // POST — create / upsert override
    // -----------------------------------------------------------------------

    @Test
    void setOverride_shouldCreateAndReturnOverride() throws Exception {
        mockMvc.perform(post("/api/admin/customers/" + customerId + "/price-overrides")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "optionId": %d,
                                  "customPrice": 299.99
                                }
                                """.formatted(optionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(customerId.intValue()))
                .andExpect(jsonPath("$.optionId").value(optionId.intValue()))
                .andExpect(jsonPath("$.customPrice").value(299.99));
    }

    @Test
    void setOverride_calledTwiceForSameOption_shouldUpdatePrice() throws Exception {
        String firstBody = """
                {
                  "optionId": %d,
                  "customPrice": 100.00
                }
                """.formatted(optionId);

        mockMvc.perform(post("/api/admin/customers/" + customerId + "/price-overrides")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstBody))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/admin/customers/" + customerId + "/price-overrides")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "optionId": %d,
                                  "customPrice": 200.00
                                }
                                """.formatted(optionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customPrice").value(200.00));

        mockMvc.perform(get("/api/admin/customers/" + customerId + "/price-overrides")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customPrice").value(200.00));
    }

    @Test
    void setOverride_forUnknownCustomerId_shouldReturn404() throws Exception {
        mockMvc.perform(post("/api/admin/customers/999999/price-overrides")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "optionId": %d,
                                  "customPrice": 100.00
                                }
                                """.formatted(optionId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void setOverride_forUnknownOptionId_shouldReturn404() throws Exception {
        mockMvc.perform(post("/api/admin/customers/" + customerId + "/price-overrides")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "optionId": 999999,
                                  "customPrice": 100.00
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void setOverride_forNonCustomerUser_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/admin/customers/" + adminUserId + "/price-overrides")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "optionId": %d,
                                  "customPrice": 100.00
                                }
                                """.formatted(optionId)))
                .andExpect(status().isBadRequest());
    }

    // -----------------------------------------------------------------------
    // DELETE — remove override
    // -----------------------------------------------------------------------

    @Test
    void deleteOverride_shouldRemoveOverrideAndConfirm() throws Exception {
        mockMvc.perform(post("/api/admin/customers/" + customerId + "/price-overrides")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "optionId": %d,
                                  "customPrice": 150.00
                                }
                                """.formatted(optionId)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/admin/customers/" + customerId + "/price-overrides/" + optionId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/customers/" + customerId + "/price-overrides")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deleteOverride_whenOverrideDoesNotExist_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/admin/customers/" + customerId + "/price-overrides/" + optionId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteOverride_forUnknownCustomerId_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/admin/customers/999999/price-overrides/" + optionId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // -----------------------------------------------------------------------
    // Security — unauthenticated requests are rejected
    // -----------------------------------------------------------------------

    @Test
    void listOverrides_withoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/admin/customers/" + customerId + "/price-overrides"))
                .andExpect(status().isUnauthorized());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private String loginAsAdmin() throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@adama.local",
                                  "password": "admin1234"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(response, "$.token");
    }

    private Long createTestCustomer() throws Exception {
        String uniqueSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String response = mockMvc.perform(post("/api/admin/customers")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Test Customer",
                                  "email": "customer-%s@example.com",
                                  "customerIdentityNumber": "%s",
                                  "phoneNumber": "0501234567",
                                  "password": "password123",
                                  "basePackagePrice": 10000
                                }
                                """.formatted(uniqueSuffix, uniqueSuffix)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number id = JsonPath.read(response, "$.id");
        return id.longValue();
    }

    private Long fetchAdminUserId() throws Exception {
        String response = mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number id = JsonPath.read(response, "$.id");
        return id.longValue();
    }

    /**
     * Inserts a PackageOption directly via the repository — no admin REST endpoint
     * exists for package options until Phase 6.
     */
    private Long createTestPackageOption() {
        PackageOption option = packageOptionRepository.save(
                PackageOption.builder()
                        .nameHe("אפשרות בדיקה")
                        .nameEn("Test Option")
                        .category(PackageOptionCategory.BRIDE_CHAIR)
                        .globalPrice(BigDecimal.valueOf(500))
                        .imageUrl("/test/bride-chair.png")
                        .visualBehavior("REPLACE_IMAGE")
                        .active(true)
                        .sortOrder(1)
                        .build()
        );
        return option.getId();
    }
}
