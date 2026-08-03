package com.example.dto;

public record SignupResponse(
    Long id,
    String username,
    String email
) {}