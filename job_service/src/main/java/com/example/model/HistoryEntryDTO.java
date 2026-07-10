package com.example.model;

import java.time.Instant;
import java.util.UUID;

public record HistoryEntryDTO(
    UUID id,
    String jobStatus,
    Instant jobStarted,
    Instant jobFinished
) {}
