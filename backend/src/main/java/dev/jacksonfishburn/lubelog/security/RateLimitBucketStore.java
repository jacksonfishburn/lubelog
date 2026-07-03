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

    /**
     * Drops every bucket that has refilled back to full capacity. A fully-replenished bucket is
     * behaviorally identical to a freshly-created one, so removing it changes nothing for the caller
     * — the next request simply rebuilds an equivalent bucket. This bounds map growth to keys seen
     * within roughly the last refill interval and is safe to run concurrently with live traffic.
     */
    public void evictFullyReplenishedBuckets() {
        buckets.values().removeIf(bucket -> bucket.getAvailableTokens() >= properties.capacity());
    }

    /** Number of buckets currently tracked. Exposed for the sweeper and tests. */
    public int trackedBucketCount() {
        return buckets.size();
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(properties.capacity())
                .refillIntervally(properties.refillTokens(), properties.refillDuration())
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
