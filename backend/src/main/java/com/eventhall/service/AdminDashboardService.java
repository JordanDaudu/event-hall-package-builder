package com.eventhall.service;

import com.eventhall.dto.AdminDashboardResponse;
import com.eventhall.dto.EventTypeRevenueDto;
import com.eventhall.dto.UpgradeUsageDto;
import com.eventhall.entity.Quote;
import com.eventhall.entity.QuoteItem;
import com.eventhall.repository.QuoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminDashboardService {

    private final QuoteRepository quoteRepository;

    public AdminDashboardService(QuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard(int year) {
        LocalDateTime start = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(year, 12, 31, 23, 59);

        List<Quote> approvedQuotes =
                quoteRepository.findByApprovedAtBetween(start, end);

        long totalQuotes = quoteRepository.count();
        long approvedCount = approvedQuotes.size();

        BigDecimal totalRevenue = approvedQuotes.stream()
                .map(Quote::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Integer, BigDecimal> revenueByMonth = new HashMap<>();

        for (int i = 1; i <= 12; i++) {
            revenueByMonth.put(i, BigDecimal.ZERO);
        }

        for (Quote quote : approvedQuotes) {
            int month = quote.getApprovedAt().getMonthValue();

            revenueByMonth.put(
                    month,
                    revenueByMonth.get(month).add(quote.getTotalPrice())
            );
        }

        Map<String, BigDecimal> revenueByEventTypeMap = new HashMap<>();

        for (Quote quote : approvedQuotes) {
            String eventTypeName = quote.getEventType().getName();

            revenueByEventTypeMap.put(
                    eventTypeName,
                    revenueByEventTypeMap
                            .getOrDefault(eventTypeName, BigDecimal.ZERO)
                            .add(quote.getTotalPrice())
            );
        }

        List<EventTypeRevenueDto> revenueByEventType =
                revenueByEventTypeMap.entrySet()
                        .stream()
                        .map(entry -> new EventTypeRevenueDto(entry.getKey(), entry.getValue()))
                        .toList();

        Map<String, Long> upgradeUsageMap = new HashMap<>();

        for (Quote quote : approvedQuotes) {
            for (QuoteItem item : quote.getQuoteItems()) {
                String upgradeName = item.getUpgrade().getName();

                upgradeUsageMap.put(
                        upgradeName,
                        upgradeUsageMap.getOrDefault(upgradeName, 0L) + 1
                );
            }
        }

        List<UpgradeUsageDto> topUpgrades =
                upgradeUsageMap.entrySet()
                        .stream()
                        .map(entry -> new UpgradeUsageDto(entry.getKey(), entry.getValue()))
                        .sorted((a, b) -> Long.compare(b.count(), a.count()))
                        .toList();

        return new AdminDashboardResponse(
                totalQuotes,
                approvedCount,
                totalRevenue,
                revenueByMonth,
                revenueByEventType,
                topUpgrades
        );
    }
}