package com.example.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import com.example.dto.SignupRequest;
import com.example.dto.SignupResponse;
import com.example.service.UserService;
import com.example.dto.LoginRequest;
import com.example.dto.LoginResponse;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/api/users/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SignupResponse handleSignupSubmit(@RequestBody SignupRequest request) {
        return userService.signup(request);
    }  
    
    @PostMapping("/api/users/login")
    public LoginResponse handleLoginSubmit(@RequestBody LoginRequest request) {
        return userService.login(request);
    }
}