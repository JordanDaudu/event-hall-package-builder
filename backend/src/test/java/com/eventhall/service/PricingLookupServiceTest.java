package com.eventhall.service;

import com.eventhall.entity.CustomerOptionPriceOverride;
import com.eventhall.entity.PackageOption;
import com.eventhall.entity.UserAccount;
import com.eventhall.repository.CustomerOptionPriceOverrideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricingLookupServiceTest {

    @Mock
    private CustomerOptionPriceOverrideRepository overrideRepository;

    private PricingLookupService pricingLookupService;

    @BeforeEach
    void setUp() {
        pricingLookupService = new PricingLookupService(overrideRepository);
    }

    @Test
    void resolvePrice_whenOverrideExists_shouldReturnCustomPrice() {
        Long customerId = 1L;
        BigDecimal globalPrice = BigDecimal.valueOf(500);
        BigDecimal customPrice = BigDecimal.valueOf(350);

        PackageOption option = PackageOption.builder()
                .id(10L)
                .nameHe("אפשרות בדיקה")
                .globalPrice(globalPrice)
                .build();

        CustomerOptionPriceOverride override = CustomerOptionPriceOverride.builder()
                .id(1L)
                .customer(UserAccount.builder().id(customerId).build())
                .packageOption(option)
                .customPrice(customPrice)
                .build();

        when(overrideRepository.findByCustomerIdAndPackageOption_Id(customerId, option.getId()))
                .thenReturn(Optional.of(override));

        BigDecimal result = pricingLookupService.resolvePrice(customerId, option);

        assertEquals(customPrice, result,
                "Should return the custom price when an override exists for the customer/option pair");
    }

    @Test
    void resolvePrice_whenNoOverrideExists_shouldReturnGlobalPrice() {
        Long customerId = 1L;
        BigDecimal globalPrice = BigDecimal.valueOf(500);

        PackageOption option = PackageOption.builder()
                .id(10L)
                .nameHe("אפשרות בדיקה")
                .globalPrice(globalPrice)
                .build();

        when(overrideRepository.findByCustomerIdAndPackageOption_Id(customerId, option.getId()))
                .thenReturn(Optional.empty());

        BigDecimal result = pricingLookupService.resolvePrice(customerId, option);

        assertEquals(globalPrice, result,
                "Should fall back to the global price when no override exists");
    }
}
