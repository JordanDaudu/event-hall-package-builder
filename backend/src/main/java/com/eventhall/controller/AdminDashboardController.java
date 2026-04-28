package com.eventhall.controller;

import com.eventhall.dto.AdminDashboardResponse;
import com.eventhall.service.AdminDashboardService;
import org.springframework.web.bind.annotation.*;

import java.time.Year;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService service;

    public AdminDashboardController(AdminDashboardService service) {
        this.service = service;
    }

    @GetMapping
    public AdminDashboardResponse getDashboard(
            @RequestParam(required = false) Integer year
    ) {
        int targetYear = year != null ? year : Year.now().getValue();
        return service.getDashboard(targetYear);
    }
}