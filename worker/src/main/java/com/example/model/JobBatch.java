package com.example.model;

import java.time.Instant;
import java.util.List;

public record JobBatch (
    Instant polledAt,
    List<JobFound> foundJobs,
    List<String> failures
) {}