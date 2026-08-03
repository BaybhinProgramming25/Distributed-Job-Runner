package com.example.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.dto.JobFound;
import com.example.repository.DashboardRepository;

@Service
public class DashboardService {

    private final DashboardRepository repository;

    public DashboardService(DashboardRepository repository) {
        this.repository = repository;
    }

    public List<JobFound> getJobsForWatchedCompanies(String username) {
        return repository.findJobsWatchedBy(username);
    }
}
