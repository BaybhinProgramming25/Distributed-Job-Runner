package com.example.dto;

import java.util.List;

public record SubscribeRequest(
    List<String> companies
) {}
