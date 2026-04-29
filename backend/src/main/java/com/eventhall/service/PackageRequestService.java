package com.eventhall.service;

import com.eventhall.dto.*;
import com.eventhall.entity.*;
import com.eventhall.enums.RequestStatus;
import com.eventhall.enums.UserRole;
import com.eventhall.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Core business logic for the package request lifecycle.
 *
 * Submission snapshots all prices at the time of the call so that
 * subsequent price changes (global or customer-specific) never affect
 * already-submitted or approved requests.
 *
 * Status transitions: PENDING → APPROVED or PENDING → REJECTED only.
 * Once decided, a request is frozen.
 */
@Service
@Transactional
public class PackageRequestService {

    private final PackageRequestRepository requestRepository;
    private final UserAccountRepository userAccountRepository;
    private final PackageOptionRepository packageOptionRepository;
    private final CustomerOptionPriceOverrideRepository overrideRepository;
    private final PricingLookupService pricingLookupService;
    private final VenueService venueService;
    private final OptionCompatibilityRuleRepository compatibilityRuleRepository;

    public PackageRequestService(
            PackageRequestRepository requestRepository,
            UserAccountRepository userAccountRepository,
            PackageOptionRepository packageOptionRepository,
            CustomerOptionPriceOverrideRepository overrideRepository,
            PricingLookupService pricingLookupService,
            VenueService venueService,
            OptionCompatibilityRuleRepository compatibilityRuleRepository
    ) {
        this.requestRepository = requestRepository;
        this.userAccountRepository = userAccountRepository;
        this.packageOptionRepository = packageOptionRepository;
        this.overrideRepository = overrideRepository;
        this.pricingLookupService = pricingLookupService;
        this.venueService = venueService;
        this.compatibilityRuleRepository = compatibilityRuleRepository;
    }

    // -----------------------------------------------------------------------
    // Customer — Submit
    // -----------------------------------------------------------------------

    /**
     * Submits a new package request on behalf of the authenticated customer.
     *
     * @param customerEmail email from the JWT principal (never from request body)
     * @param req           the validated request body
     * @return full detail response of the newly created request
     */
    public PackageRequestDetailResponse submit(String customerEmail, SubmitRequestRequest req) {
        UserAccount customer = requireActiveCustomer(customerEmail);

        // Validate and snapshot venue
        Venue venue = venueService.requireActiveVenue(req.venueId());

        List<PackageRequestItem> items = new ArrayList<>();
        BigDecimal optionTotal = BigDecimal.ZERO;

        // ── 1. Validate and snapshot the main chuppah ──────────────────────
        PackageOption chuppah = packageOptionRepository.findById(req.chuppahOptionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "חופה לא נמצאה: " + req.chuppahOptionId()));
        if (!chuppah.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "החופה \"" + chuppah.getNameHe() + "\" אינה זמינה כרגע");
        }
        if (chuppah.getCategory() != PackageOptionCategory.CHUPPAH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "האפשרות שנבחרה אינה חופה ראשית");
        }
        optionTotal = optionTotal.add(snapshotItem(customer, chuppah, items));

        // ── 2. Validate and snapshot chuppah upgrades ──────────────────────
        List<Long> upgradeIds = req.safeUpgradeIds();
        long distinctUpgrades = upgradeIds.stream().distinct().count();
        if (distinctUpgrades != upgradeIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "רשימת תוספות החופה מכילה כפילויות");
        }
        for (Long upgradeId : upgradeIds) {
            PackageOption upgrade = packageOptionRepository.findById(upgradeId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "תוספת חופה לא נמצאה: " + upgradeId));
            if (!upgrade.isActive()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "תוספת החופה \"" + upgrade.getNameHe() + "\" אינה זמינה כרגע");
            }
            if (upgrade.getCategory() != PackageOptionCategory.CHUPPAH_UPGRADE) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "\"" + upgrade.getNameHe() + "\" אינה תוספת חופה");
            }
            // Enforce compatibility rule
            if (!compatibilityRuleRepository.existsByParentOption_IdAndChildOption_IdAndActiveTrue(
                    chuppah.getId(), upgradeId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "תוספת החופה \"" + upgrade.getNameHe() + "\" אינה תואמת לחופה שנבחרה");
            }
            optionTotal = optionTotal.add(snapshotItem(customer, upgrade, items));
        }

        // ── 3. Validate and snapshot aisle (optional) ──────────────────────
        if (req.aisleOptionId() != null) {
            PackageOption aisle = packageOptionRepository.findById(req.aisleOptionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "שדרה לא נמצאה: " + req.aisleOptionId()));
            if (!aisle.isActive()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "השדרה \"" + aisle.getNameHe() + "\" אינה זמינה כרגע");
            }
            if (aisle.getCategory() != PackageOptionCategory.AISLE) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "האפשרות שנבחרה אינה שדרה");
            }
            optionTotal = optionTotal.add(snapshotItem(customer, aisle, items));
        }

        // ── 4. Validate and snapshot remaining options ──────────────────────
        List<Long> otherOptionIds = req.safeOptionIds();
        long distinctOther = otherOptionIds.stream().distinct().count();
        if (distinctOther != otherOptionIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "רשימת האפשרויות מכילה כפילויות. כל אפשרות יכולה להופיע פעם אחת בלבד");
        }
        // Guard against chuppah/upgrade being passed again in optionIds
        for (Long optId : otherOptionIds) {
            if (optId.equals(req.chuppahOptionId()) || upgradeIds.contains(optId)
                    || (req.aisleOptionId() != null && optId.equals(req.aisleOptionId()))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "אפשרות " + optId + " כבר נכללת בחלקים אחרים של הבקשה");
            }
        }
        for (Long optionId : otherOptionIds) {
            PackageOption option = packageOptionRepository.findById(optionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "אפשרות חבילה לא נמצאה: " + optionId));
            if (!option.isActive()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "אפשרות החבילה \"" + option.getNameHe() + "\" אינה זמינה כרגע");
            }
            optionTotal = optionTotal.add(snapshotItem(customer, option, items));
        }

        // ── 5. Total ────────────────────────────────────────────────────────
        BigDecimal basePrice = customer.getBasePackagePrice();
        if (basePrice == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "מחיר הבסיס של הלקוח לא הוגדר. אנא פנה לצוות האולם.");
        }
        BigDecimal totalPrice = basePrice.add(optionTotal);

        PackageRequest packageRequest = PackageRequest.builder()
                .customer(customer)
                .venue(venue)
                .status(RequestStatus.PENDING)
                .eventCustomerIdentityNumber(req.eventCustomerIdentityNumber())
                .eventContactName(req.eventContactName())
                .eventContactPhoneNumber(req.eventContactPhoneNumber())
                .eventDate(req.eventDate())
                .venueNameSnapshot(venue.getNameHe())
                .basePackagePriceSnapshot(basePrice)
                .totalPrice(totalPrice)
                .knightTableCount(req.knightTableCount())
                .submittedAt(Instant.now())
                .build();

        // Wire items back to the request before saving
        for (PackageRequestItem item : items) {
            item.setPackageRequest(packageRequest);
        }
        packageRequest.getItems().addAll(items);

        PackageRequest saved = requestRepository.save(packageRequest);
        return toDetail(saved);
    }

    // -----------------------------------------------------------------------
    // Customer — Read own requests
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<PackageRequestSummaryResponse> listByCustomer(String customerEmail) {
        UserAccount customer = requireExistingCustomer(customerEmail);
        return requestRepository.findByCustomer_IdOrderBySubmittedAtDesc(customer.getId())
                .stream()
                .map(PackageRequestSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PackageRequestDetailResponse getByIdForCustomer(String customerEmail, Long requestId) {
        UserAccount customer = requireExistingCustomer(customerEmail);
        PackageRequest request = requestRepository
                .findByIdAndCustomerId(requestId, customer.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "בקשה לא נמצאה"));
        return toDetail(request);
    }

    // -----------------------------------------------------------------------
    // Admin — Read all requests
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<PackageRequestSummaryResponse> listAll(RequestStatus statusFilter) {
        List<PackageRequest> requests = statusFilter != null
                ? requestRepository.findAllByStatusOrderBySubmittedAtDesc(statusFilter)
                : requestRepository.findAllByOrderBySubmittedAtDesc();
        return requests.stream()
                .map(PackageRequestSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PackageRequestDetailResponse getByIdForAdmin(Long requestId) {
        PackageRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "בקשה לא נמצאה"));
        return toDetail(request);
    }

    // -----------------------------------------------------------------------
    // Admin — Update status
    // -----------------------------------------------------------------------

    /**
     * Approves or rejects a request. Only PENDING requests can be decided.
     * Throws 409 Conflict if the request has already been decided.
     */
    public PackageRequestDetailResponse updateStatus(Long requestId, AdminUpdateStatusRequest req) {
        if (req.status() == RequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "לא ניתן לשנות סטטוס חזרה ל-PENDING");
        }

        PackageRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "בקשה לא נמצאה"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "הבקשה כבר טופלה ולא ניתן לשנות את סטטוסה");
        }

        request.setStatus(req.status());
        request.setSummaryNotes(req.summaryNotes());
        request.setDecidedAt(Instant.now());

        return toDetail(requestRepository.save(request));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private PackageRequestDetailResponse toDetail(PackageRequest request) {
        List<PackageRequestItemResponse> itemResponses = request.getItems()
                .stream()
                .map(PackageRequestItemResponse::from)
                .toList();
        return PackageRequestDetailResponse.from(request, itemResponses);
    }

    /**
     * Resolves pricing for one option, creates a snapshot item, adds it to the list,
     * and returns the finalPrice so the caller can accumulate the total.
     */
    private BigDecimal snapshotItem(UserAccount customer, PackageOption option,
                                    List<PackageRequestItem> items) {
        BigDecimal globalPrice = option.getGlobalPrice();
        var maybeOverride = overrideRepository
                .findByCustomerIdAndPackageOption_Id(customer.getId(), option.getId());
        BigDecimal overridePrice = maybeOverride.map(o -> o.getCustomPrice()).orElse(null);
        BigDecimal finalPrice = pricingLookupService.resolvePrice(customer.getId(), option);

        items.add(PackageRequestItem.builder()
                .packageOption(option)
                .optionNameSnapshot(option.getNameHe())
                .globalPriceSnapshot(globalPrice)
                .customerOverridePriceSnapshot(overridePrice)
                .finalPrice(finalPrice)
                .build());
        return finalPrice;
    }

    private UserAccount requireActiveCustomer(String email) {
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "משתמש לא נמצא"));
        if (user.getRole() != UserRole.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "רק לקוחות יכולים לשלוח בקשות חבילה");
        }
        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "החשבון שלך אינו פעיל. אנא פנה לצוות האולם");
        }
        return user;
    }

    private UserAccount requireExistingCustomer(String email) {
        return userAccountRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "משתמש לא נמצא"));
    }
}
