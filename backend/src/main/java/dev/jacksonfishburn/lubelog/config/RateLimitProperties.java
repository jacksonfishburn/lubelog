package dev.jacksonfishburn.lubelog.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds the {@code app.ratelimit.*} configuration for the global per-user API rate limiter.
 *
 * @param enabled        whether the rate-limit filter enforces limits (pass-through when false)
 * @param capacity       maximum number of tokens a single bucket can hold
 * @param refillTokens   number of tokens added to a bucket each refill interval
 * @param refillDuration length of the refill interval (e.g. {@code PT1M} for one minute)
 */
@ConfigurationProperties(prefix = "app.ratelimit")
public record RateLimitProperties(
        boolean enabled,
        long capacity,
        long refillTokens,
        Duration refillDuration) {
}
