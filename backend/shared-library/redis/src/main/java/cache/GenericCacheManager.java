package cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generic, reusable cache manager for ANY entity and ANY user.
 *
 * Pattern: Cache-Aside (Lazy Loading)
 *   - Read  → check cache first, fallback to DB, then populate cache
 *   - Write → invalidate ALL user pages (bulk delete via tag set)
 *
 * Key Structure:
 *   {entity}:user:{userId}:page:{p}:count:{c}  → paginated list data
 *   {entity}:user:{userId}:id:{id}             → single record data
 *   {entity}:id:{id}                           → global single record (not user scoped)
 *   {entity}:user:{userId}:keys                → tag set tracking all keys for this user
 *   {entity}:keys                              → tag set tracking all global keys
 *
 * Usage:
 *   cache.getPaged("recipe", userId, page, count)
 *   cache.putPaged("recipe", userId, page, count, data)
 *   cache.invalidateUser("recipe", userId)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GenericCacheManager {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheProperties cacheProperties;

    // ─────────────────────────────────────────────────────────────
    // Key Builders — all key patterns defined in one place
    // ─────────────────────────────────────────────────────────────

    /**
     * Key for a paginated list scoped to a specific user.
     * Example: recipe:user:1:page:2:count:10
     */
    private String pagedKey(String entity, long userId, int page, int count) {
        return String.format("%s:user:%d:page:%d:count:%d", entity, userId, page, count);
    }

    /**
     * Key for a single record scoped to a specific user.
     * Example: recipe:user:1:id:45
     */
    private String singleKey(String entity, long userId, long id) {
        return String.format("%s:user:%d:id:%d", entity, userId, id);
    }

    /**
     * Key for a single record NOT scoped to any user (e.g. categories, public data).
     * Example: category:id:3
     */
    private String globalSingleKey(String entity, long id) {
        return String.format("%s:id:%d", entity, id);
    }

    /**
     * Tag set key that tracks ALL cache keys belonging to a specific user.
     * Used for bulk invalidation — delete this set to find all keys to wipe.
     * Example: recipe:user:1:keys
     */
    private String tagKey(String entity, long userId) {
        return String.format("%s:user:%d:keys", entity, userId);
    }

    /**
     * Tag set key that tracks ALL cache keys for a global (non-user-scoped) entity.
     * Example: category:keys
     */
    private String globalTagKey(String entity) {
        return String.format("%s:keys", entity);
    }

    // ─────────────────────────────────────────────────────────────
    // GET Operations
    // ─────────────────────────────────────────────────────────────

    /**
     * Fetch a paginated list from cache for a specific user.
     * Returns empty if cache miss — caller should then fetch from DB and call putPaged().
     */
    public <T> Optional<T> getPaged(String entity, long userId, int page, int count) {
        return get(pagedKey(entity, userId, page, count));
    }

    /**
     * Fetch a single record from cache for a specific user.
     * Returns empty if cache miss — caller should then fetch from DB and call putSingle().
     */
    public <T> Optional<T> getSingle(String entity, long userId, long id) {
        return get(singleKey(entity, userId, id));
    }

    /**
     * Fetch a single global record from cache (not user scoped).
     * Returns empty if cache miss — caller should then fetch from DB and call putGlobal().
     */
    public <T> Optional<T> getGlobal(String entity, long id) {
        return get(globalSingleKey(entity, id));
    }

    // ─────────────────────────────────────────────────────────────
    // PUT Operations
    // ─────────────────────────────────────────────────────────────

    /**
     * Store a paginated list in cache for a specific user.
     * Also registers this key in the user's tag set for future bulk invalidation.
     */
    public void putPaged(String entity, long userId, int page, int count, Object data) {
        put(pagedKey(entity, userId, page, count), data, tagKey(entity, userId));
    }

    /**
     * Store a single record in cache for a specific user.
     * Also registers this key in the user's tag set for future bulk invalidation.
     */
    public void putSingle(String entity, long userId, long id, Object data) {
        put(singleKey(entity, userId, id), data, tagKey(entity, userId));
    }

    /**
     * Store a single global record in cache (not user scoped).
     * Also registers this key in the global tag set for future bulk invalidation.
     */
    public void putGlobal(String entity, long id, Object data) {
        put(globalSingleKey(entity, id), data, globalTagKey(entity));
    }

    // ─────────────────────────────────────────────────────────────
    // INVALIDATE Operations
    // ─────────────────────────────────────────────────────────────

    /**
     * Wipe ALL cached data for a specific user (all pages + all single records).
     *
     * Call this after: INSERT, UPDATE, DELETE
     *
     * How it works:
     *   1. Read the user's tag set → get all tracked cache keys
     *   2. Delete all those keys + the tag set itself in one Redis call
     */
    public void invalidateUser(String entity, long userId) {
        invalidate(tagKey(entity, userId), entity, userId);
    }

    /**
     * Wipe ALL cached data for a global entity (not user scoped).
     * Call this after any write to a globally shared entity (e.g. categories).
     */
    public void invalidateGlobal(String entity) {
        invalidate(globalTagKey(entity), entity, null);
    }

    /**
     * Wipe only one specific record from cache (user scoped).
     *
     * NOTE: Only use this when you are certain the paginated list
     * does NOT need to be refreshed (e.g. a field update that does
     * not affect ordering or count). For INSERT and DELETE, always
     * use invalidateUser() instead, since page structure changes.
     */
    public void invalidateSingle(String entity, long userId, long id) {
        try {
            String key = singleKey(entity, userId, id);
            Boolean deleted = redisTemplate.delete(key);
            log.debug("Cache invalidated single → entity={} userId={} id={} deleted={}",
                    entity, userId, id, deleted);
        } catch (Exception e) {
            log.error("Cache single invalidation failed → entity={} id={}", entity, id, e);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Private Core Helpers
    // ─────────────────────────────────────────────────────────────

    /**
     * Core GET — fetches a value from Redis by key.
     * Returns Optional.empty() on cache miss or Redis failure (graceful degradation).
     * Caller falls back to DB on empty result.
     */
    @SuppressWarnings("unchecked")
    private <T> Optional<T> get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            log.debug("Cache {} → key={}", value != null ? "HIT " : "MISS", key);
            return Optional.ofNullable((T) value);
        } catch (Exception e) {
            // Redis failure must never break the main application flow
            log.error("Cache GET failed → key={}", key, e);
            return Optional.empty();
        }
    }

    /**
     * Core PUT — stores a value in Redis with TTL.
     * Also registers the key in the tag set so it can be bulk-deleted later.
     *
     * Tag set TTL is slightly longer than entry TTL to ensure
     * the tag set outlives all the keys it tracks.
     */
    private void put(String key, Object data, String tagKey) {
        try {
            Duration ttl    = Duration.ofMinutes(cacheProperties.getTtlMinutes());
            Duration tagTtl = Duration.ofMinutes(cacheProperties.getTagTtlMinutes());

            // Store the actual data with TTL
            redisTemplate.opsForValue().set(key, data, ttl);

            // Register this key in the tag set for future bulk invalidation
            redisTemplate.opsForSet().add(tagKey, key);

            // Refresh tag set TTL so it stays alive as long as any entry exists
            redisTemplate.expire(tagKey, tagTtl);

            log.debug("Cache SET → key={} ttl={}min", key, cacheProperties.getTtlMinutes());
        } catch (Exception e) {
            // Cache failure must never break the main application flow
            log.error("Cache PUT failed → key={}", key, e);
        }
    }

    /**
     * Core INVALIDATE — bulk deletes all keys tracked in a tag set.
     *
     * Steps:
     *   1. Read all members of the tag set (these are all the cache keys for this user/entity)
     *   2. Add the tag set key itself to the deletion list
     *   3. Delete everything in a single Redis call
     *
     * Single Redis call = no race condition between reading and deleting keys.
     */
    private void invalidate(String tagKey, String entity, Long userId) {
        try {
            // Step 1: Get all tracked cache keys from the tag set
            Set<Object> members = redisTemplate.opsForSet().members(tagKey);

            if (members != null && !members.isEmpty()) {
                // Step 2: Build deletion list — all tracked keys + the tag set itself
                List<String> keysToDelete = members.stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());

                keysToDelete.add(tagKey);

                // Step 3: Delete everything in one Redis call
                Long deleted = redisTemplate.delete(keysToDelete);
                log.debug("Cache invalidated → entity={} userId={} keys deleted={}",
                        entity, userId, deleted);
            } else {
                // No tracked keys found — just clean up the empty tag set
                redisTemplate.delete(tagKey);
                log.debug("Cache invalidated → entity={} userId={} (no keys found)",
                        entity, userId);
            }

        } catch (Exception e) {
            // Cache failure must never break the main application flow
            log.error("Cache invalidation failed → entity={} userId={}", entity, userId, e);
        }
    }
}