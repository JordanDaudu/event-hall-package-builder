package com.eventhall.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record AdminDashboardResponse(
        long totalQuotes,
        long approvedQuotes,
        BigDecimal totalRevenue,
        Map<Integer, BigDecimal> revenueByMonth,
        List<EventTypeRevenueDto> revenueByEventType,
        List<UpgradeUsageDto> topUpgrades
) {}