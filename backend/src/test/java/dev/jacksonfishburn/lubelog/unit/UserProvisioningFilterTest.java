package dev.jacksonfishburn.lubelog.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import dev.jacksonfishburn.lubelog.security.UserProvisioningFilter;
import dev.jacksonfishburn.lubelog.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class UserProvisioningFilterTest {

    private static final String KEYCLOAK_ID = "11111111-1111-1111-1111-111111111111";
    private static final String EMAIL = "user@example.com";

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private UserProvisioningFilter filter;

    @BeforeEach
    void setUp() {
        filter = new UserProvisioningFilter(userService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_provisionsUser_whenJwtAuthenticationTokenIsPresent() throws Exception {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(KEYCLOAK_ID)
                .claim("email", EMAIL)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of()));

        filter.doFilter(request, response, filterChain);

        verify(userService).provisionIfAbsent(eq(KEYCLOAK_ID), eq(EMAIL));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_skipsProvisioning_whenAuthenticationIsNull() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(null);

        filter.doFilter(request, response, filterChain);

        verify(userService, never()).provisionIfAbsent(any(), any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_skipsProvisioning_whenAuthenticationIsNotAJwtToken() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user", "password", List.of()));

        filter.doFilter(request, response, filterChain);

        verify(userService, never()).provisionIfAbsent(any(), any());
        verify(filterChain).doFilter(request, response);
    }
}
