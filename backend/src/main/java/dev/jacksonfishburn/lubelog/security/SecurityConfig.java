package dev.jacksonfishburn.lubelog.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import dev.jacksonfishburn.lubelog.service.UserService;

/**
 * Production security configuration. Requires a valid Keycloak-issued JWT on every request
 * except the health endpoint.
 *
 * <p>The bean is disabled via {@code app.security.test-override.enabled=true}, which lets
 * {@code support.TestSecurityConfig} register a replacement filter chain for integration tests
 * without using {@code @Profile}. When real Keycloak wiring is finished, this class does not
 * need to change for that mechanism to keep working.
 */
@Configuration
public class SecurityConfig {

    @Bean
    @ConditionalOnProperty(name = "app.security.test-override.enabled", havingValue = "false", matchIfMissing = true)
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserService userService) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .addFilterAfter(new UserProvisioningFilter(userService), BearerTokenAuthenticationFilter.class);
        return http.build();
    }
}
