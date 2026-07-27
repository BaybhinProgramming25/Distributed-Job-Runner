package com.example.dto;

public record SignupResponse(
    Long userId,
    String username,
    String token
) {}