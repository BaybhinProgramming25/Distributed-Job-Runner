package com.example.dto;

// What user sends 
public record LoginRequest(
    
) {}

// What we send back
public record LoginResponse(
    String token
) {}