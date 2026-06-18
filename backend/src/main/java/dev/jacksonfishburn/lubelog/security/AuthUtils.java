package dev.jacksonfishburn.lubelog.security;

import dev.jacksonfishburn.lubelog.entity.User;
import dev.jacksonfishburn.lubelog.exception.AccessDeniedException;
import dev.jacksonfishburn.lubelog.exception.ResourceNotFoundException;
import dev.jacksonfishburn.lubelog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUtils {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            String keycloakId = jwtAuthentication.getToken().getSubject();
            return userRepository.findByKeycloakId(keycloakId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", keycloakId));
        }
        throw new AccessDeniedException();
    }
}