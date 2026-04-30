package com.br3akPoint.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * RedisService
 *
 * Central helper for all Redis operations.
 *
 * Two flavours of template are injected:
 *   • redisTemplate        → stores any Object serialised as JSON
 *   • stringRedisTemplate  → stores plain String values (no JSON overhead)
 *
 * Every public method is null-safe and swallows exceptions gracefully so a
 * Redis outage never crashes the application — callers receive Optional.empty()
 * or false instead.
 */
@Slf4j
@Service
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public RedisService(RedisTemplate<String, Object> redisTemplate, StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisTemplate = redisTemplate;
    }

    // =========================================================================
    // Object (JSON) operations — RedisTemplate<String, Object>
    // =========================================================================

    /**
     * Store any serialisable object without an expiry (persists until eviction).
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            log.debug("Redis SET key={}", key);
            return true;
        } catch (Exception e) {
            log.error("Redis SET failed key={}", key, e);
            return false;
        }
    }

    /**
     * Store any serialisable object with an explicit TTL.
     *
     * @param key      Redis key
     * @param value    any JSON-serialisable object
     * @param duration expiry duration (e.g. Duration.ofMinutes(30))
     */
    public boolean set(String key, Object value, Duration duration) {
        try {
            redisTemplate.opsForValue().set(key, value, duration);
            log.debug("Redis SET key={} ttl={}", key, duration);
            return true;
        } catch (Exception e) {
            log.error("Redis SET (with TTL) failed key={}", key, e);
            return false;
        }
    }

    /**
     * Store with the application-wide default TTL from application.yml.
     */
    public boolean setWithDefaultTtl(String key, Object value) {
        long defaultTtlSeconds = 3600;
        return set(key, value, Duration.ofSeconds(defaultTtlSeconds));
    }

    /**
     * Retrieve a value and cast to the expected type.
     *
     * @return Optional containing the value, or empty if absent / on error
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return Optional.empty();
            }
            return Optional.of(type.cast(value));
        } catch (Exception e) {
            log.error("Redis GET failed key={}", key, e);
            return Optional.empty();
        }
    }

    /**
     * Retrieve a raw Object (when you don't know the type at compile time).
     */
    public Optional<Object> get(String key) {
        try {
            return Optional.ofNullable(redisTemplate.opsForValue().get(key));
        } catch (Exception e) {
            log.error("Redis GET failed key={}", key, e);
            return Optional.empty();
        }
    }

    // =========================================================================
    // String operations — StringRedisTemplate (no JSON overhead)
    // =========================================================================

    /**
     * Store a plain String value without expiry.
     */
    public boolean setString(String key, String value) {
        try {
            stringRedisTemplate.opsForValue().set(key, value);
            log.debug("Redis SET (string) key={}", key);
            return true;
        } catch (Exception e) {
            log.error("Redis SET (string) failed key={}", key, e);
            return false;
        }
    }

    /**
     * Store a plain String value with an explicit TTL.
     */
    public boolean setString(String key, String value, Duration duration) {
        try {
            stringRedisTemplate.opsForValue().set(key, value, duration);
            log.debug("Redis SET (string) key={} ttl={}", key, duration);
            return true;
        } catch (Exception e) {
            log.error("Redis SET (string, TTL) failed key={}", key, e);
            return false;
        }
    }

    /**
     * Retrieve a plain String value.
     */
    public Optional<String> getString(String key) {
        try {
            return Optional.ofNullable(stringRedisTemplate.opsForValue().get(key));
        } catch (Exception e) {
            log.error("Redis GET (string) failed key={}", key, e);
            return Optional.empty();
        }
    }

    // =========================================================================
    // TTL / Expiry management
    // =========================================================================

    /**
     * Update (or set) the expiry on an existing key without touching the value.
     */
    public void expire(String key, Duration duration) {
        try {
            Boolean result = redisTemplate.expire(key, duration.toSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Redis EXPIRE failed key={}", key, e);
        }
    }

    /**
     * Return the remaining TTL for a key, or empty if the key doesn't exist /
     * has no expiry.
     */
    public Optional<Duration> getTtl(String key) {
        try {
            Long seconds = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (seconds == null || seconds < 0) {
                return Optional.empty();          // -1 = no expiry, -2 = missing
            }
            return Optional.of(Duration.ofSeconds(seconds));
        } catch (Exception e) {
            log.error("Redis TTL failed key={}", key, e);
            return Optional.empty();
        }
    }

    // =========================================================================
    // Existence & deletion
    // =========================================================================

    /** Returns true if the key exists in Redis. */
    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Redis EXISTS failed key={}", key, e);
            return false;
        }
    }

    /** Delete a single key. Returns true if the key was present and removed. */
    public boolean delete(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        } catch (Exception e) {
            log.error("Redis DELETE failed key={}", key, e);
            return false;
        }
    }

    /**
     * Delete all keys matching a glob pattern (e.g. "myapp:user:*").
     * <p>
     * ⚠️  Uses KEYS — avoid on large keyspaces in production; prefer SCAN-based
     * deletion for high-cardinality patterns.
     *
     * @return number of deleted keys
     */
    public long deleteByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys == null || keys.isEmpty()) {
                return 0;
            }
            Long count = redisTemplate.delete(keys);
            log.info("Redis KEYS DELETE pattern={} deleted={}", pattern, count);
            return count == null ? 0 : count;
        } catch (Exception e) {
            log.error("Redis KEYS DELETE failed pattern={}", pattern, e);
            return 0;
        }
    }

    // =========================================================================
    // Atomic increment / decrement (useful for counters / rate-limiting)
    // =========================================================================

    /**
     * Atomically increment a counter. Creates the key with value 1 if absent.
     */
    public long increment(String key) {
        try {
            Long result = redisTemplate.opsForValue().increment(key);
            return result == null ? 0 : result;
        } catch (Exception e) {
            log.error("Redis INCR failed key={}", key, e);
            return 0;
        }
    }

    /**
     * Increment and set expiry in a single logical operation (expire is applied
     * only on the first increment so the window stays fixed).
     */
    public long incrementWithExpiry(String key, Duration duration) {
        long value = increment(key);
        if (value == 1) {
            expire(key, duration);          // only set TTL when key is brand-new
        }
        return value;
    }

    /** Atomically decrement a counter. */
    public long decrement(String key) {
        try {
            Long result = redisTemplate.opsForValue().decrement(key);
            return result == null ? 0 : result;
        } catch (Exception e) {
            log.error("Redis DECR failed key={}", key, e);
            return 0;
        }
    }

    // =========================================================================
    // Set-if-absent (distributed lock / idempotency guard)
    // =========================================================================

    /**
     * SET NX — stores the value only if the key does not already exist.
     *
     * @return true  if the key was absent and has now been set
     *         false if the key already existed (value unchanged)
     */
    public boolean setIfAbsent(String key, Object value, Duration duration) {
        try {
            Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, duration);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Redis SETNX failed key={}", key, e);
            return false;
        }
    }
}