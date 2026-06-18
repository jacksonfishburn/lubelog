package dev.jacksonfishburn.lubelog.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import dev.jacksonfishburn.lubelog.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Runs after {@code BearerTokenAuthenticationFilter} so the {@link JwtAuthenticationToken} is
 * already in the {@link SecurityContextHolder}. Ensures a local {@code users} row exists for the
 * authenticated Keycloak identity before the request reaches the controller.
 */
@RequiredArgsConstructor
public class UserProvisioningFilter extends OncePerRequestFilter {

    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            String keycloakId = jwtAuthentication.getToken().getSubject();
            String email = jwtAuthentication.getToken().getClaimAsString("email");
            userService.provisionIfAbsent(keycloakId, email);
        }
        filterChain.doFilter(request, response);
    }
}
