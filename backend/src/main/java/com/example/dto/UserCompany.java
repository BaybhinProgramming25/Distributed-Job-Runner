package com.example.dto;

import java.util.List;

// Might need to play around with this later on 
public record UserCompany(
    String username,
    List<String> companies
) {}