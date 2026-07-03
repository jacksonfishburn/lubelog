package dev.jacksonfishburn.lubelog.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.jacksonfishburn.lubelog.config.RateLimitProperties;
import dev.jacksonfishburn.lubelog.security.RateLimitBucketStore;
import io.github.bucket4j.Bucket;

class RateLimitBucketStoreTest {

    private static final long CAPACITY = 5;

    private RateLimitBucketStore store;

    @BeforeEach
    void setUp() {
        store = new RateLimitBucketStore(
                new RateLimitProperties(true, CAPACITY, CAPACITY, Duration.ofMinutes(1)));
    }

    @Test
    void resolveBucket_returnsSameBucketForSameKey() {
        Bucket first = store.resolveBucket("user:abc");
        Bucket second = store.resolveBucket("user:abc");

        assertThat(first).isSameAs(second);
        assertThat(store.trackedBucketCount()).isEqualTo(1);
    }

    @Test
    void evictFullyReplenishedBuckets_removesUntouchedBuckets() {
        store.resolveBucket("user:idle");

        store.evictFullyReplenishedBuckets();

        assertThat(store.trackedBucketCount()).isZero();
    }

    @Test
    void evictFullyReplenishedBuckets_keepsBucketsWithConsumedTokens() {
        store.resolveBucket("user:active").tryConsume(1);

        store.evictFullyReplenishedBuckets();

        assertThat(store.trackedBucketCount()).isEqualTo(1);
    }
}
