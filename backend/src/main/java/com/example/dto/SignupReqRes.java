package com.example.dto;

// Structure of what the user sends to use 
public record SignupRequest(
    
)

// What we send back
public record SignupResponse(
    Long userId,
    String username,
    String token
) {}