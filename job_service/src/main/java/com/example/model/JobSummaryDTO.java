package com.example.model;

import java.time.Instant;
import java.util.UUID;

public record JobSummaryDTO(
    UUID id,
    String schedule,
    int retriesCount,
    int maxRetries,
    Instant nextRun,
    String jobActive,
    String status
) {}
