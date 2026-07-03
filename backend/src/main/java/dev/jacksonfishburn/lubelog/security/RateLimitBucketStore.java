package dev.jacksonfishburn.lubelog.security;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import dev.jacksonfishburn.lubelog.config.RateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;

/**
 * Holds one in-memory {@link Bucket} per rate-limit key (typically {@code "user:<sub>"} or
 * {@code "ip:<addr>"}). Buckets are created lazily on first use and never evicted — acceptable for
 * a single VPS instance at this scale. State is not shared across instances; a distributed backend
 * (e.g. Redis) would be required if this ever runs multi-instance.
 */
@Component
@RequiredArgsConstructor
public class RateLimitBucketStore {

    private final RateLimitProperties properties;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, ignored -> newBucket());
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(properties.capacity())
                .refillIntervally(properties.refillTokens(), properties.refillDuration())
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
