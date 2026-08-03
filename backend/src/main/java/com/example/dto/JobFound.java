package com.example.dto;

import java.time.OffsetDateTime;

public record JobFound(
    String company,
    String ats,
    String title,
    String location,
    String department,
    String url,
    String posted,
    OffsetDateTime firstSeen
) {}