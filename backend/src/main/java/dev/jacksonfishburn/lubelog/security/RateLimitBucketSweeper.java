package dev.jacksonfishburn.lubelog.security;

import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Periodically reclaims idle rate-limit buckets so the in-memory map cannot grow unbounded under
 * heavy or hostile traffic (e.g. many distinct source IPs). Only fully-replenished buckets are
 * removed, so eviction never affects an active limit — see
 * {@link RateLimitBucketStore#evictFullyReplenishedBuckets()}.
 */
@Component
@RequiredArgsConstructor
public class RateLimitBucketSweeper {

    private final RateLimitBucketStore bucketStore;

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void sweep() {
        bucketStore.evictFullyReplenishedBuckets();
    }
}
