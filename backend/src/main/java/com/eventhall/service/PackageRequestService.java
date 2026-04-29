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
    private final PackageRequestItemRepository itemRepository;
    private final UserAccountRepository userAccountRepository;
    private final PackageOptionRepository packageOptionRepository;
    private final CustomerOptionPriceOverrideRepository overrideRepository;
    private final VenueService venueService;

    public PackageRequestService(
            PackageRequestRepository requestRepository,
            PackageRequestItemRepository itemRepository,
            UserAccountRepository userAccountRepository,
            PackageOptionRepository packageOptionRepository,
            CustomerOptionPriceOverrideRepository overrideRepository,
            VenueService venueService
    ) {
        this.requestRepository = requestRepository;
        this.itemRepository = itemRepository;
        this.userAccountRepository = userAccountRepository;
        this.packageOptionRepository = packageOptionRepository;
        this.overrideRepository = overrideRepository;
        this.venueService = venueService;
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

        // Validate and snapshot each selected option
        List<PackageRequestItem> items = new ArrayList<>();
        BigDecimal optionTotal = BigDecimal.ZERO;

        for (Long optionId : req.optionIds()) {
            PackageOption option = packageOptionRepository.findById(optionId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "אפשרות חבילה לא נמצאה: " + optionId));
            if (!option.isActive()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "אפשרות החבילה \"" + option.getNameHe() + "\" אינה זמינה כרגע");
            }

            // Resolve override snapshot
            var maybeOverride = overrideRepository
                    .findByCustomerIdAndPackageOption_Id(customer.getId(), optionId);

            BigDecimal globalPrice = option.getGlobalPrice();
            BigDecimal overridePrice = maybeOverride
                    .map(o -> o.getCustomPrice())
                    .orElse(null);
            BigDecimal finalPrice = overridePrice != null ? overridePrice : globalPrice;

            optionTotal = optionTotal.add(finalPrice);

            items.add(PackageRequestItem.builder()
                    .packageOption(option)
                    .optionNameSnapshot(option.getNameHe())
                    .globalPriceSnapshot(globalPrice)
                    .customerOverridePriceSnapshot(overridePrice)
                    .finalPrice(finalPrice)
                    .build());
        }

        // Snapshot base price (null-safe: treat null as 0)
        BigDecimal basePrice = customer.getBasePackagePrice() != null
                ? customer.getBasePackagePrice()
                : BigDecimal.ZERO;
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
