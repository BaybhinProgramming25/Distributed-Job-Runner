package com.example.model;

import java.sql.Timestamp;
import java.util.UUID;

public record JobHistory(
    UUID JobId,
    UUID HistoryId,
    String Schedule,
    int retriesCount,
    int maxRetries,
    Timestamp nextRun
) {}
