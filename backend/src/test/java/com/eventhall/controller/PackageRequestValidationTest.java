package com.eventhall.controller;

import com.eventhall.entity.OptionCompatibilityRule;
import com.eventhall.entity.PackageOption;
import com.eventhall.repository.OptionCompatibilityRuleRepository;
import com.eventhall.repository.PackageOptionRepository;
import com.eventhall.testutil.TestDataFactory;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Focused validation tests for table design within a package request submission.
 *
 * Covers:
 *  - Regular table frame, primary flower size, primary flower option (required fields)
 *  - Flower size mismatch validation
 *  - Secondary flower constraints (only with LARGE primary, must be SMALL)
 *  - Candle mode: RANDOM and SELECTED rules
 *  - Table context enforcement (REGULAR / KNIGHT / BOTH)
 *  - Knight table count constraints
 *
 * Uses @DirtiesContext(AFTER_CLASS) for isolation.
 * Uses @TestInstance(PER_CLASS) so @BeforeAll can be non-static and use @Autowired beans.
 * @BeforeAll creates shared package options once; @BeforeEach creates a fresh customer per test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PackageRequestValidationTest {

    @Autowired MockMvc mockMvc;
    @Autowired PackageOptionRepository packageOptionRepository;
    @Autowired OptionCompatibilityRuleRepository compatibilityRuleRepository;

    private String adminToken;
    private long venueId;
    private long chuppahId;

    // Table frames
    private long bothFrameId;
    private long regularOnlyFrameId;
    private long knightOnlyFrameId;

    // Table flowers
    private long largeFlowerBothId;
    private long smallFlowerBothId;
    private long largeFlowerRegularId;
    private long smallFlowerKnightId;

    // Table candles
    private long candleBothId;
    private long candleRegularId;
    private long candleKnightId;

    // Per-test customer state (set in @BeforeEach)
    private String customerToken;
    private int customerCounter = 0;

    @BeforeAll
    void setUpAll() throws Exception {
        adminToken = loginAsAdmin();

        // Get venue from seeded data
        String venuesJson = mockMvc.perform(get("/api/venues"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        venueId = ((Number) JsonPath.read(venuesJson, "$[0].id")).longValue();

        // Chuppah (no upgrades needed for these tests)
        chuppahId = packageOptionRepository.save(
                TestDataFactory.chuppah("חופה בדיקה", BigDecimal.valueOf(500))).getId();

        // Table frames
        bothFrameId = packageOptionRepository.save(
                TestDataFactory.tableFrame("מסגרת BOTH", BigDecimal.valueOf(100), "BOTH")).getId();
        regularOnlyFrameId = packageOptionRepository.save(
                TestDataFactory.tableFrame("מסגרת REGULAR בלבד", BigDecimal.valueOf(100), "REGULAR")).getId();
        knightOnlyFrameId = packageOptionRepository.save(
                TestDataFactory.tableFrame("מסגרת KNIGHT בלבד", BigDecimal.valueOf(100), "KNIGHT")).getId();

        // Table flowers
        largeFlowerBothId = packageOptionRepository.save(
                TestDataFactory.tableFlower("פרח גדול BOTH", BigDecimal.valueOf(80), "LARGE", "BOTH")).getId();
        smallFlowerBothId = packageOptionRepository.save(
                TestDataFactory.tableFlower("פרח קטן BOTH", BigDecimal.valueOf(60), "SMALL", "BOTH")).getId();
        largeFlowerRegularId = packageOptionRepository.save(
                TestDataFactory.tableFlower("פרח גדול REGULAR", BigDecimal.valueOf(80), "LARGE", "REGULAR")).getId();
        smallFlowerKnightId = packageOptionRepository.save(
                TestDataFactory.tableFlower("פרח קטן KNIGHT", BigDecimal.valueOf(60), "SMALL", "KNIGHT")).getId();

        // Table candles
        candleBothId = packageOptionRepository.save(
                TestDataFactory.tableCandle("נר BOTH", BigDecimal.valueOf(50), "BOTH")).getId();
        candleRegularId = packageOptionRepository.save(
                TestDataFactory.tableCandle("נר REGULAR", BigDecimal.valueOf(50), "REGULAR")).getId();
        candleKnightId = packageOptionRepository.save(
                TestDataFactory.tableCandle("נר KNIGHT", BigDecimal.valueOf(50), "KNIGHT")).getId();
    }

    @BeforeEach
    void setUpCustomer() throws Exception {
        customerCounter++;
        String suffix = "val" + customerCounter + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        String email = "valcust-" + suffix + "@test.com";
        createCustomer(adminToken, suffix, email);
        customerToken = loginAs(email, "test1234");
    }

    // -----------------------------------------------------------------------
    // Required fields — frame, flower size, flower option
    // -----------------------------------------------------------------------

    @Test
    void regularTable_frameRequired_shouldReturn400() throws Exception {
        String body = regularDesign(null, "\"LARGE\"", largeFlowerBothId, null, "\"RANDOM\"", "[]");
        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void regularTable_primaryFlowerSizeRequired_shouldReturn400() throws Exception {
        String body = regularDesign(bothFrameId, "null", largeFlowerBothId, null, "\"RANDOM\"", "[]");
        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void regularTable_primaryFlowerOptionRequired_shouldReturn400() throws Exception {
        String body = regularDesign(bothFrameId, "\"LARGE\"", null, null, "\"RANDOM\"", "[]");
        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // -----------------------------------------------------------------------
    // Flower size mismatch
    // -----------------------------------------------------------------------

    @Test
    void regularTable_primaryFlowerSizeLarge_butOptionIsSmall_shouldReturn400() throws Exception {
        // primaryFlowerSize = LARGE but option.flowerSize = SMALL — mismatch
        String body = regularDesign(bothFrameId, "\"LARGE\"", smallFlowerBothId, null, "\"RANDOM\"", "[]");
        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void regularTable_primaryFlowerSizeSmall_butOptionIsLarge_shouldReturn400() throws Exception {
        // primaryFlowerSize = SMALL but option.flowerSize = LARGE — mismatch
        String body = regularDesign(bothFrameId, "\"SMALL\"", largeFlowerBothId, null, "\"RANDOM\"", "[]");
        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // -----------------------------------------------------------------------
    // Secondary flower constraints
    // -----------------------------------------------------------------------

    @Test
    void regularTable_secondaryFlower_withSmallPrimarySize_shouldReturn400() throws Exception {
        // Secondary flower is only allowed when primaryFlowerSize = LARGE
        String body = regularDesign(bothFrameId, "\"SMALL\"", smallFlowerBothId, smallFlowerBothId, "\"RANDOM\"", "[]");
        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void regularTable_secondaryFlower_mustBeSmall_notLarge_shouldReturn400() throws Exception {
        // Secondary flower must have flowerSize = SMALL; providing a LARGE one should fail
        String body = regularDesign(bothFrameId, "\"LARGE\"", largeFlowerBothId, largeFlowerBothId, "\"RANDOM\"", "[]");
        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void regularTable_secondaryFlower_withLargeAndSmallCorrect_shouldReturn201() throws Exception {
        // primaryFlowerSize = LARGE, secondary flower is SMALL — valid
        String body = regularDesign(bothFrameId, "\"LARGE\"", largeFlowerBothId, smallFlowerBothId, "\"RANDOM\"", "[]");
        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    // -----------------------------------------------------------------------
    // Candle mode: RANDOM
    // -----------------------------------------------------------------------

    @Test
    void regularTable_randomCandle_emptyIds_shouldReturn201() throws Exception {
        String body = regularDesign(bothFrameId, "\"LARGE\"", largeFlowerBothId, null, "\"RANDOM\"", "[]");
        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    // -----------------------------------------------------------------------
    // Candle mode: SELECTED
    // -----------------------------------------------------------------------

    @Test
    void regularTable_selectedCandle_emptyList_shouldReturn400() throws Exception {
        String body = regularDesign(bothFrameId, "\"LARGE\"", largeFlowerBothId, null, "\"SELECTED\"", "[]");
        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void regularTable_selectedCandle_fourIds_shouldReturn400() throws Exception {
        // Max 3 candles when SELECTED; 4 should fail
        String ids = candleBothId + ", " + candleBothId + ", " + candleBothId + ", " + candleBothId;
        String body = regularDesign(bothFrameId, "\"LARGE\"", largeFlowerBothId, null,
                "\"SELECTED\"", "[" + ids + "]");
        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void regularTable_selectedCandle_oneId_shouldReturn201() throws Exception {
        String body = regularDesign(bothFrameId, "\"LARGE\"", largeFlowerBothId, null,
                "\"SELECTED\"", "[" + candleBothId + "]");
        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void regularTable_invalidCandleMode_shouldReturn400() throws Exception {
        String body = regularDesign(bothFrameId, "\"LARGE\"", largeFlowerBothId, null, "\"MAYBE\"", "[]");
        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // -----------------------------------------------------------------------
    // Table context enforcement
    // -----------------------------------------------------------------------

    @Test
    void regularTable_rejectsKnightOnlyFrame_shouldReturn400() throws Exception {
        String body = regularDesign(knightOnlyFrameId, "\"LARGE\"", largeFlowerBothId, null, "\"RANDOM\"", "[]");
        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void regularTable_acceptsRegularOnlyFrame_shouldReturn201() throws Exception {
        // REGULAR-only frame in regular table design — valid
        String body = regularDesign(regularOnlyFrameId, "\"LARGE\"", largeFlowerRegularId, null, "\"RANDOM\"", "[]");
        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void regularTable_rejectsKnightOnlyCandle_shouldReturn400() throws Exception {
        // KNIGHT-only candle used in regular table — must fail
        String body = regularDesign(bothFrameId, "\"LARGE\"", largeFlowerBothId, null,
                "\"SELECTED\"", "[" + candleKnightId + "]");
        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void regularTable_acceptsRegularOnlyCandle_shouldReturn201() throws Exception {
        String body = regularDesign(bothFrameId, "\"LARGE\"", largeFlowerBothId, null,
                "\"SELECTED\"", "[" + candleRegularId + "]");
        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    // -----------------------------------------------------------------------
    // Knight table count constraints
    // -----------------------------------------------------------------------

    @Test
    void knightTableCount_zero_withNullDesign_shouldReturn201() throws Exception {
        String body = fullRequest(
                chuppahId, bothFrameId,
                "\"LARGE\"", largeFlowerBothId, "null", "\"RANDOM\"", "[]",
                0, null,
                null, null, null, null, null
        );
        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void knightTableCount_positive_withoutKnightDesign_shouldReturn400() throws Exception {
        String body = fullRequest(
                chuppahId, bothFrameId,
                "\"LARGE\"", largeFlowerBothId, "null", "\"RANDOM\"", "[]",
                1, null,
                null, null, null, null, null
        );
        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void knightTableCount_exceedingMax_shouldReturn400() throws Exception {
        // Max is 4 per SubmitRequestRequest @Max constraint
        String body = fullRequest(
                chuppahId, bothFrameId,
                "\"LARGE\"", largeFlowerBothId, "null", "\"RANDOM\"", "[]",
                5, null,
                null, null, null, null, null
        );
        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void knightTable_rejectsRegularOnlyFrame_shouldReturn400() throws Exception {
        // KNIGHT design that uses a REGULAR-only frame — must fail
        String knightDesign = """
                {
                  "frameOptionId": %d,
                  "primaryFlowerSize": "LARGE",
                  "primaryFlowerOptionId": %d,
                  "secondarySmallFlowerOptionId": null,
                  "candleSelectionMode": "RANDOM",
                  "candleHolderOptionIds": []
                }
                """.formatted(regularOnlyFrameId, largeFlowerBothId);

        String body = fullRequest(
                chuppahId, bothFrameId,
                "\"LARGE\"", largeFlowerBothId, "null", "\"RANDOM\"", "[]",
                1, knightDesign,
                null, null, null, null, null
        );
        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void knightTable_withValidKnightDesign_shouldReturn201() throws Exception {
        String knightDesign = """
                {
                  "frameOptionId": %d,
                  "primaryFlowerSize": "LARGE",
                  "primaryFlowerOptionId": %d,
                  "secondarySmallFlowerOptionId": null,
                  "candleSelectionMode": "RANDOM",
                  "candleHolderOptionIds": []
                }
                """.formatted(knightOnlyFrameId, largeFlowerBothId);

        String body = fullRequest(
                chuppahId, bothFrameId,
                "\"LARGE\"", largeFlowerBothId, "null", "\"RANDOM\"", "[]",
                1, knightDesign,
                null, null, null, null, null
        );
        mockMvc.perform(post("/api/customer/requests")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Builds a POST /api/customer/requests JSON body with a custom regular table design.
     * All optional fields use null defaults (no upgrades, no knight table).
     *
     * @param frameId         nullable Long — null serializes as JSON null
     * @param primarySizeJson JSON literal: "\"LARGE\"", "\"SMALL\"", or "null"
     * @param primaryFlower   nullable Long for primaryFlowerOptionId
     * @param secondaryFlower nullable Long for secondarySmallFlowerOptionId
     * @param candleModeJson  JSON literal: "\"RANDOM\"", "\"SELECTED\"", etc.
     * @param candleIdsJson   JSON array literal: "[]" or "[1, 2]"
     */
    private String regularDesign(
            Long frameId,
            String primarySizeJson,
            Long primaryFlower,
            Long secondaryFlower,
            String candleModeJson,
            String candleIdsJson
    ) {
        String frameStr = frameId == null ? "null" : frameId.toString();
        String primaryFlowerStr = primaryFlower == null ? "null" : primaryFlower.toString();
        String secondaryStr = secondaryFlower == null ? "null" : secondaryFlower.toString();

        return """
                {
                  "venueId": %d,
                  "chuppahOptionId": %d,
                  "chuppahUpgradeIds": [],
                  "aisleOptionId": null,
                  "regularTableDesign": {
                    "frameOptionId": %s,
                    "primaryFlowerSize": %s,
                    "primaryFlowerOptionId": %s,
                    "secondarySmallFlowerOptionId": %s,
                    "candleSelectionMode": %s,
                    "candleHolderOptionIds": %s
                  },
                  "knightTableCount": 0,
                  "knightTableDesign": null,
                  "napkinOptionId": null,
                  "tableclothOptionId": null,
                  "brideChairOptionId": null,
                  "eventCustomerIdentityNumber": "987654321",
                  "eventContactName": "ישראל ישראלי",
                  "eventContactPhoneNumber": "0501234567",
                  "eventDate": "2030-06-15"
                }
                """.formatted(
                venueId, chuppahId,
                frameStr, primarySizeJson, primaryFlowerStr, secondaryStr,
                candleModeJson, candleIdsJson
        );
    }

    /**
     * Builds a fully parametrised request body including optional knight table design.
     * knightDesignJson should be a JSON object string, or null for no knight design.
     */
    private String fullRequest(
            long chuppah, long regularFrame,
            String regularFlowerSize, long regularFlowerId,
            String regularSecondary, String candleMode, String candleIds,
            int knightCount, String knightDesignJson,
            Long napkin, Long tablecloth, Long brideChair, Long aisle, Long unused
    ) {
        String knightBlock = knightDesignJson != null ? knightDesignJson : "null";
        String napkinStr = napkin != null ? napkin.toString() : "null";
        String tableclothStr = tablecloth != null ? tablecloth.toString() : "null";
        String brideChairStr = brideChair != null ? brideChair.toString() : "null";
        String aisleStr = aisle != null ? aisle.toString() : "null";

        return """
                {
                  "venueId": %d,
                  "chuppahOptionId": %d,
                  "chuppahUpgradeIds": [],
                  "aisleOptionId": %s,
                  "regularTableDesign": {
                    "frameOptionId": %d,
                    "primaryFlowerSize": %s,
                    "primaryFlowerOptionId": %d,
                    "secondarySmallFlowerOptionId": %s,
                    "candleSelectionMode": %s,
                    "candleHolderOptionIds": %s
                  },
                  "knightTableCount": %d,
                  "knightTableDesign": %s,
                  "napkinOptionId": %s,
                  "tableclothOptionId": %s,
                  "brideChairOptionId": %s,
                  "eventCustomerIdentityNumber": "555666777",
                  "eventContactName": "ישראל ישראלי",
                  "eventContactPhoneNumber": "0501234567",
                  "eventDate": "2030-06-15"
                }
                """.formatted(
                venueId, chuppah,
                aisleStr,
                regularFrame, regularFlowerSize, regularFlowerId,
                regularSecondary, candleMode, candleIds,
                knightCount, knightBlock,
                napkinStr, tableclothStr, brideChairStr
        );
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

    private void createCustomer(String token, String suffix, String email) throws Exception {
        mockMvc.perform(post("/api/admin/customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Val Customer",
                                  "email": "%s",
                                  "customerIdentityNumber": "%s",
                                  "phoneNumber": "0501234567",
                                  "password": "test1234",
                                  "basePackagePrice": 5000
                                }
                                """.formatted(email, suffix)))
                .andExpect(status().isCreated());
    }
}
