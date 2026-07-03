package dev.jacksonfishburn.lubelog.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import dev.jacksonfishburn.lubelog.config.RateLimitProperties;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Global per-user rate limiter for {@code /api/**}. Runs after {@code UserProvisioningFilter} so the
 * {@link JwtAuthenticationToken} is already in the {@link SecurityContextHolder}, letting requests be
 * keyed by the Keycloak {@code sub} claim without touching the database. Unauthenticated requests
 * fall back to the remote address.
 *
 * <p>Because this filter executes before the {@code DispatcherServlet}, thrown exceptions never reach
 * {@code GlobalExceptionHandler}. On rejection it therefore writes the 429 response body directly, in
 * the same {@code {status, message}} shape produced by {@code GlobalExceptionHandler.ErrorResponse}.
 */
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String RATE_LIMIT_MESSAGE = "Rate limit exceeded. Please try again later.";

    private final RateLimitBucketStore bucketStore;
    private final RateLimitProperties properties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/") || path.startsWith("/api/dev/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!properties.enabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        Bucket bucket = bucketStore.resolveBucket(resolveKey(request));
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
            return;
        }

        writeTooManyRequests(response);
    }

    private String resolveKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return "user:" + jwtAuthentication.getToken().getSubject();
        }
        return "ip:" + request.getRemoteAddr();
    }

    private void writeTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"status\":" + HttpStatus.TOO_MANY_REQUESTS.value()
                        + ",\"message\":\"" + RATE_LIMIT_MESSAGE + "\"}");
    }
}
