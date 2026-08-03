package com.example.dto;

public record User(
    Long id,
    String username,
    String email,
    String passwordHashed,
    String phoneNumber
) {}