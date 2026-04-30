package com.eventhall.controller;

import com.eventhall.entity.OptionCompatibilityRule;
import com.eventhall.entity.PackageOption;
import com.eventhall.entity.Venue;
import com.eventhall.repository.OptionCompatibilityRuleRepository;
import com.eventhall.repository.PackageOptionRepository;
import com.eventhall.repository.VenueRepository;
import com.eventhall.testutil.TestDataFactory;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the customer package request flow and admin request management.
 *
 * Uses @DirtiesContext(AFTER_CLASS) for full isolation.
 * Uses @TestInstance(PER_CLASS) so @BeforeAll can be non-static and use @Autowired beans.
 *
 * Seeded data available in this context:
 *   - admin@adama.local / admin1234 (UserAccountSeeder)
 *   - Two active venues (VenueSeeder)
 *   - Generic package options: CATERING, DECORATION, MUSIC, PHOTOGRAPHY, EXTRAS (PackageOptionSeeder)
 *   - Four CHUPPAH_UPGRADE options, no compatibility rules (ChuppahCompatibilitySeeder — no CHUPPAHs found)
 *
 * Test-specific options (CHUPPAH, TABLE_FRAME, TABLE_FLOWER, etc.) are created
 * in @BeforeAll via the repository directly.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CustomerPackageRequestControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired PackageOptionRepository packageOptionRepository;
    @Autowired VenueRepository venueRepository;
    @Autowired OptionCompatibilityRuleRepository compatibilityRuleRepository;

    private String adminToken;
    private String customerToken;
    private Long customerId;
    private String customerEmail;
    private long venueId;
    private long inactiveVenueId;
    private long chuppahId;
    private long upgradeId;
    private long incompatibleUpgradeId;
    private long inactiveChuppahId;
    private long aisleId;
    private long frameId;
    private long largeFlowerId;
    private long smallFlowerId;
    private long napkinId;
    private long tableclothId;
    private long brideChairId;

    @BeforeAll
    void setUpAll() throws Exception {
        adminToken = loginAsAdmin();

        // --- Venues ---
        String venuesJson = mockMvc.perform(get("/api/venues"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        venueId = ((Number) JsonPath.read(venuesJson, "$[0].id")).longValue();

        // Create an inactive venue for negative tests
        Venue inactive = venueRepository.save(Venue.builder()
                .nameHe("אולם לא פעיל")
                .nameEn("Inactive Hall")
                .active(false)
                .sortOrder(99)
                .build());
        inactiveVenueId = inactive.getId();

        // --- Package options ---
        chuppahId = packageOptionRepository.save(
                TestDataFactory.chuppah("חופה בדיקה", BigDecimal.valueOf(1000))).getId();

        upgradeId = packageOptionRepository.save(
                TestDataFactory.chuppahUpgrade("תוספת בדיקה תואמת", BigDecimal.valueOf(500))).getId();

        incompatibleUpgradeId = packageOptionRepository.save(
                TestDataFactory.chuppahUpgrade("תוספת לא תואמת", BigDecimal.valueOf(300))).getId();

        PackageOption inactiveChuppah = TestDataFactory.chuppah("חופה לא פעילה", BigDecimal.valueOf(800));
        inactiveChuppah.setActive(false);
        inactiveChuppahId = packageOptionRepository.save(inactiveChuppah).getId();

        aisleId = packageOptionRepository.save(
                TestDataFactory.aisle("שדרה בדיקה", BigDecimal.valueOf(300))).getId();

        frameId = packageOptionRepository.save(
                TestDataFactory.tableFrame("מסגרת בדיקה", BigDecimal.valueOf(200), "BOTH")).getId();

        largeFlowerId = packageOptionRepository.save(
                TestDataFactory.tableFlower("פרח גדול בדיקה", BigDecimal.valueOf(150), "LARGE", "BOTH")).getId();

        smallFlowerId = packageOptionRepository.save(
                TestDataFactory.tableFlower("פרח קטן בדיקה", BigDecimal.valueOf(100), "SMALL", "BOTH")).getId();

        napkinId = packageOptionRepository.save(
                TestDataFactory.napkin("מפית בדיקה", BigDecimal.valueOf(50))).getId();

        tableclothId = packageOptionRepository.save(
                TestDataFactory.tablecloth("מפה בדיקה", BigDecimal.valueOf(80))).getId();

        brideChairId = packageOptionRepository.save(
                TestDataFactory.brideChair("כיסא בדיקה", BigDecimal.valueOf(200))).getId();

        // --- Compatibility rule: chuppahId → upgradeId ---
        PackageOption chuppahOpt = packageOptionRepository.findById(chuppahId).orElseThrow();
        PackageOption upgradeOpt = packageOptionRepository.findById(upgradeId).orElseThrow();
        compatibilityRuleRepository.save(OptionCompatibilityRule.builder()
                .parentOption(chuppahOpt)
                .childOption(upgradeOpt)
                .active(true)
                .build());

        // --- Customer ---
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        customerEmail = "customer-" + suffix + "@test.com";
        customerId = createCustomer(adminToken, suffix, customerEmail);
        customerToken = loginAs(customerEmail, "test1234");
    }

    // -----------------------------------------------------------------------
    // POST /api/customer/requests — submission
    // -----------------------------------------------------------------------

    @Test
    void submitRequest_withFullValidPayload_shouldReturn201WithPendingStatus() throws Exception {
        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.customerId").value(customerId.intValue()));
    }

    @Test
    void submitRequest_snapshotsVenueNameAndEventDetails() throws Exception {
        // Get the expected venue name from the first seeded venue
        String venuesJson = mockMvc.perform(get("/api/venues"))
                .andReturn().getResponse().getContentAsString();
        String expectedVenueName = JsonPath.read(venuesJson, "$[0].nameHe");

        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.venueNameSnapshot").value(expectedVenueName))
                .andExpect(jsonPath("$.eventContactName").value("ישראל ישראלי"))
                .andExpect(jsonPath("$.eventDate").value("2030-01-01"))
                .andExpect(jsonPath("$.basePackagePriceSnapshot").value(10000));
    }

    @Test
    void submitRequest_snapshotsOptionNamesInItems() throws Exception {
        String response = mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Items should include the chuppah, frame, and large flower snapshots
        List<String> names = JsonPath.read(response, "$.items[*].optionNameSnapshot");
        assertThat(names, hasItem("חופה בדיקה"));
        assertThat(names, hasItem("מסגרת בדיקה"));
        assertThat(names, hasItem("פרח גדול בדיקה"));
    }

    @Test
    void submitRequest_withInactiveVenue_shouldReturn400() throws Exception {
        String body = validRequest().replace(
                "\"venueId\": " + venueId,
                "\"venueId\": " + inactiveVenueId);
        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitRequest_withUnknownVenueId_shouldReturn4xx() throws Exception {
        String body = validRequest().replace(
                "\"venueId\": " + venueId,
                "\"venueId\": 999999");
        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void submitRequest_withIncompatibleChuppahUpgrade_shouldReturn400() throws Exception {
        String body = """
                {
                  "venueId": %d,
                  "chuppahOptionId": %d,
                  "chuppahUpgradeIds": [%d],
                  "aisleOptionId": null,
                  "regularTableDesign": {
                    "frameOptionId": %d,
                    "primaryFlowerSize": "LARGE",
                    "primaryFlowerOptionId": %d,
                    "secondarySmallFlowerOptionId": null,
                    "candleSelectionMode": "RANDOM",
                    "candleHolderOptionIds": []
                  },
                  "knightTableCount": 0,
                  "knightTableDesign": null,
                  "napkinOptionId": null,
                  "tableclothOptionId": null,
                  "brideChairOptionId": null,
                  "eventCustomerIdentityNumber": "111223344",
                  "eventContactName": "ישראל ישראלי",
                  "eventContactPhoneNumber": "0501234567",
                  "eventDate": "2030-01-01"
                }
                """.formatted(venueId, chuppahId, incompatibleUpgradeId, frameId, largeFlowerId);

        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitRequest_withDuplicateUpgradeIds_shouldReturn400() throws Exception {
        String body = """
                {
                  "venueId": %d,
                  "chuppahOptionId": %d,
                  "chuppahUpgradeIds": [%d, %d],
                  "aisleOptionId": null,
                  "regularTableDesign": {
                    "frameOptionId": %d,
                    "primaryFlowerSize": "LARGE",
                    "primaryFlowerOptionId": %d,
                    "secondarySmallFlowerOptionId": null,
                    "candleSelectionMode": "RANDOM",
                    "candleHolderOptionIds": []
                  },
                  "knightTableCount": 0,
                  "knightTableDesign": null,
                  "napkinOptionId": null,
                  "tableclothOptionId": null,
                  "brideChairOptionId": null,
                  "eventCustomerIdentityNumber": "111223355",
                  "eventContactName": "ישראל ישראלי",
                  "eventContactPhoneNumber": "0501234567",
                  "eventDate": "2030-01-01"
                }
                """.formatted(venueId, chuppahId, upgradeId, upgradeId, frameId, largeFlowerId);

        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitRequest_withInactiveChuppah_shouldReturn400() throws Exception {
        String body = validRequest().replace(
                "\"chuppahOptionId\": " + chuppahId,
                "\"chuppahOptionId\": " + inactiveChuppahId);
        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitRequest_withChuppahUpgradeAsMainChuppah_shouldReturn400() throws Exception {
        String body = validRequest().replace(
                "\"chuppahOptionId\": " + chuppahId,
                "\"chuppahOptionId\": " + upgradeId);
        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitRequest_withCompatibleUpgrade_shouldReturn201() throws Exception {
        String body = """
                {
                  "venueId": %d,
                  "chuppahOptionId": %d,
                  "chuppahUpgradeIds": [%d],
                  "aisleOptionId": %d,
                  "regularTableDesign": {
                    "frameOptionId": %d,
                    "primaryFlowerSize": "LARGE",
                    "primaryFlowerOptionId": %d,
                    "secondarySmallFlowerOptionId": %d,
                    "candleSelectionMode": "RANDOM",
                    "candleHolderOptionIds": []
                  },
                  "knightTableCount": 0,
                  "knightTableDesign": null,
                  "napkinOptionId": %d,
                  "tableclothOptionId": %d,
                  "brideChairOptionId": %d,
                  "eventCustomerIdentityNumber": "111223366",
                  "eventContactName": "ישראל ישראלי",
                  "eventContactPhoneNumber": "0501234567",
                  "eventDate": "2030-01-01"
                }
                """.formatted(venueId, chuppahId, upgradeId, aisleId, frameId, largeFlowerId,
                        smallFlowerId, napkinId, tableclothId, brideChairId);

        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    // -----------------------------------------------------------------------
    // GET /api/customer/requests — list own requests
    // -----------------------------------------------------------------------

    @Test
    void customerCanListOwnRequests() throws Exception {
        // Submit a request first to ensure there is at least one
        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", not(empty())));
    }

    // -----------------------------------------------------------------------
    // GET /api/customer/requests/{id}
    // -----------------------------------------------------------------------

    @Test
    void customerCanGetOwnRequestById() throws Exception {
        String created = mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long requestId = ((Number) JsonPath.read(created, "$.id")).longValue();

        mockMvc.perform(get("/api/customer/requests/" + requestId)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId));
    }

    @Test
    void customerGets404ForOtherCustomersRequest() throws Exception {
        // Create a second customer
        String otherSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String otherEmail = "other-" + otherSuffix + "@test.com";
        createCustomer(adminToken, otherSuffix, otherEmail);
        String otherToken = loginAs(otherEmail, "test1234");

        // Other customer submits a request
        String created = mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long otherId = ((Number) JsonPath.read(created, "$.id")).longValue();

        // First customer tries to access other's request — must get 404
        mockMvc.perform(get("/api/customer/requests/" + otherId)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isNotFound());
    }

    // -----------------------------------------------------------------------
    // GET /api/admin/requests — admin list
    // -----------------------------------------------------------------------

    @Test
    void adminCanListAllRequests() throws Exception {
        // Ensure at least one request exists
        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/admin/requests")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    void adminCanGetRequestById() throws Exception {
        String created = mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long requestId = ((Number) JsonPath.read(created, "$.id")).longValue();

        mockMvc.perform(get("/api/admin/requests/" + requestId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    // -----------------------------------------------------------------------
    // PATCH /api/admin/requests/{id}/status
    // -----------------------------------------------------------------------

    @Test
    void adminCanApproveRequest() throws Exception {
        long requestId = submitAndGetId();

        mockMvc.perform(patch("/api/admin/requests/" + requestId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "APPROVED", "summaryNotes": null}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.approvedAt").isNotEmpty());
    }

    @Test
    void adminCanRejectRequestWithNotes() throws Exception {
        long requestId = submitAndGetId();

        mockMvc.perform(patch("/api/admin/requests/" + requestId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "REJECTED", "summaryNotes": "לא מתאים לתאריך"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.summaryNotes").value("לא מתאים לתאריך"))
                .andExpect(jsonPath("$.rejectedAt").isNotEmpty());
    }

    @Test
    void adminGets409WhenApprovingAlreadyApprovedRequest() throws Exception {
        long requestId = submitAndGetId();

        // First decision — approve
        mockMvc.perform(patch("/api/admin/requests/" + requestId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "APPROVED", "summaryNotes": null}
                                """))
                .andExpect(status().isOk());

        // Second decision — conflict
        mockMvc.perform(patch("/api/admin/requests/" + requestId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "REJECTED", "summaryNotes": "changed mind"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void adminGets400WhenResettingStatusToPending() throws Exception {
        long requestId = submitAndGetId();

        mockMvc.perform(patch("/api/admin/requests/" + requestId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "PENDING", "summaryNotes": null}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminCanFilterRequestsByStatus() throws Exception {
        long requestId = submitAndGetId();

        // Approve it
        mockMvc.perform(patch("/api/admin/requests/" + requestId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "APPROVED", "summaryNotes": null}
                                """))
                .andExpect(status().isOk());

        // Filter by APPROVED — our request should appear
        mockMvc.perform(get("/api/admin/requests?status=APPROVED")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int) requestId)));

        // Filter by PENDING — our request should NOT appear
        mockMvc.perform(get("/api/admin/requests?status=PENDING")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", not(hasItem((int) requestId))));
    }

    // -----------------------------------------------------------------------
    // Security — role enforcement
    // -----------------------------------------------------------------------

    @Test
    void customerGets403ForAdminRequestEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/requests")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedGets401ForCustomerRequests() throws Exception {
        mockMvc.perform(get("/api/customer/requests"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminGets403ForCustomerRequestEndpoints() throws Exception {
        mockMvc.perform(get("/api/customer/requests")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private String validRequest() {
        return """
                {
                  "venueId": %d,
                  "chuppahOptionId": %d,
                  "chuppahUpgradeIds": [],
                  "aisleOptionId": null,
                  "regularTableDesign": {
                    "frameOptionId": %d,
                    "primaryFlowerSize": "LARGE",
                    "primaryFlowerOptionId": %d,
                    "secondarySmallFlowerOptionId": null,
                    "candleSelectionMode": "RANDOM",
                    "candleHolderOptionIds": []
                  },
                  "knightTableCount": 0,
                  "knightTableDesign": null,
                  "napkinOptionId": null,
                  "tableclothOptionId": null,
                  "brideChairOptionId": null,
                  "eventCustomerIdentityNumber": "111222333",
                  "eventContactName": "ישראל ישראלי",
                  "eventContactPhoneNumber": "0501234567",
                  "eventDate": "2030-01-01"
                }
                """.formatted(venueId, chuppahId, frameId, largeFlowerId);
    }

    private long submitAndGetId() throws Exception {
        String response = mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private String loginAsAdmin() throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "admin@adama.local", "password": "admin1234"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.token");
    }

    private String loginAs(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.token");
    }

    private Long createCustomer(String token, String suffix, String email) throws Exception {
        String response = mockMvc.perform(post("/api/admin/customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Test Customer",
                                  "email": "%s",
                                  "customerIdentityNumber": "%s",
                                  "phoneNumber": "0501234567",
                                  "password": "test1234",
                                  "basePackagePrice": 10000
                                }
                                """.formatted(email, suffix)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }
}
