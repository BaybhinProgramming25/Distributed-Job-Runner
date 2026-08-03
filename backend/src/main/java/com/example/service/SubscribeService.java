package com.example.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dto.User;
import com.example.repository.SubscribeRepository;
import com.example.repository.UserRepository;

import java.util.List;

@Service
public class SubscribeService {

    private final SubscribeRepository subscribeRepository;
    private final UserRepository userRepository;

    public SubscribeService(SubscribeRepository subscribeRepository, UserRepository userRepository) {
        this.subscribeRepository = subscribeRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void subscribeToCompanies(String username, List<String> companies) {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalStateException("User not found: " + username));
        subscribeRepository.saveUserCompanies(user.id(), companies);
    }

}
