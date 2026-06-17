package dev.jacksonfishburn.lubelog.support;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Replaces {@code SecurityConfig}'s filter chain in tests, activated by
 * {@code app.security.test-override.enabled=true}. Permits all requests and seeds the
 * {@link SecurityContextHolder} with a {@link JwtAuthenticationToken} whose subject matches
 * {@link #TEST_USER_SUBJECT}, mirroring a real Keycloak-issued token's {@code sub} claim.
 *
 * <p>When real Keycloak auth is wired up, delete this class (and stop setting the override
 * property) — no production code or test method bodies need to change.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestSecurityConfig {

    public static final String TEST_USER_SUBJECT = "11111111-1111-1111-1111-111111111111";

    @Bean
    @ConditionalOnProperty(name = "app.security.test-override.enabled", havingValue = "true")
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(new MockJwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private static class MockJwtAuthenticationFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            Jwt jwt = Jwt.withTokenValue("test-token")
                    .header("alg", "none")
                    .subject(TEST_USER_SUBJECT)
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();
            SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of()));
            filterChain.doFilter(request, response);
        }
    }
}
