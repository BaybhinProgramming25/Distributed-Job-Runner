package com.example.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import com.example.dto.JobFound;
import com.example.service.DashboardService;

@RestController 
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/api/dashboard")
    public List<JobFound> getDashboard(Authentication authentication) {
        return dashboardService.getJobsForWatchedCompanies(authentication.getName());
    }
}
