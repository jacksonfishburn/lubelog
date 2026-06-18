package dev.jacksonfishburn.lubelog.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import dev.jacksonfishburn.lubelog.entity.User;
import dev.jacksonfishburn.lubelog.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void provisionIfAbsent(String keycloakId, String email) {
        if (userRepository.findByKeycloakId(keycloakId).isPresent()) {
            return;
        }
        try {
            userRepository.save(User.builder()
                    .keycloakId(keycloakId)
                    .email(email)
                    .build());
        } catch (DataIntegrityViolationException e) {
            // Concurrent first request already provisioned this user; unique constraint on keycloak_id protects the DB.
        }
    }
}
