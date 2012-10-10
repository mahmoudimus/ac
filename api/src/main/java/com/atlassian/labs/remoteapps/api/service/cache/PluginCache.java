package com.atlassian.labs.remoteapps.api.service.cache;

import com.atlassian.util.concurrent.Promise;

import java.util.Map;

/**
 * Proposed interface for plugin cache to be backed by memcached or redis when running remotely or via a weakly-key
 * map when running within the Atlassian application.  One cache instance per plugin, though the memcached (or other)
 * backing may be one instance shared among plugins with automatically namespaced keys.
 *
 * Missing features:
 *  * Transactions
 *  * Compare-and-set
 *  * Atomic list mutation
 *  * Memcached/Redis/whatever specific features
 */
public interface PluginCache
{
    /**
     * Adds an item to the cache iff it doesn't already exist
     * @param key The cache key
     * @param exp The expiration in milliseconds from now
     * @param o The object to be serialized
     * @return A promise of boolean whether it was successful or not
     */
    Promise<Boolean> add(String key, int exp, Object o);

    /**
     * Sets an item in the cache
     * @param key The cache key
     * @param exp The expiration in milliseconds from now
     * @param o The object to be serialized
     * @return A promise of boolean whether it was successful or not
     */
    Promise<Boolean> set(String key, int exp, Object o);

    /**
     * Replaces an item to the cache iff it already exists
     * @param key The cache key
     * @param exp The expiration in milliseconds from now
     * @param o The object to be serialized
     * @return A promise of boolean whether it was successful or not
     */
    Promise<Boolean> replace(String key, int exp, Object o);

    /**
     * Gets an item from the cache
     * @param key The cache key
     * @return A promise of the de-serialized object
     */
    Promise<Object> get(String key);

    /**
     * Gets items from the cache
     * @param keys The cache keys
     * @return A promise of the de-serialized objects
     */
    BulkPromise<Map<String, Object>> getBulk(Iterable<String> keys);

    /**
     * Gets items from the cache
     * @param keys The cache keys
     * @return A promise of the de-serialized objects
     */
    BulkPromise<Map<String, Object>> getBulk(String... keys);

    /**
     * Resets the expiration of an item
     * @param key The cache key
     * @param exp The new expiration in milliseconds from now
     * @return A promise of the de-serialized objects
     */
    Promise<Boolean> touch(final String key, final int exp);

    /**
     * Increments an numeric cache value atomically
     * @param key The cache key
     * @param by The number to increment it by
     * @return A promise of the result
     */
    Promise<Long> increment(String key, int by);

    /**
     * Decrements an numeric cache value atomically
     * @param key The cache key
     * @param by The number to decrement it by
     * @return A promise of the result
     */
    Promise<Long> decrement(String key, int by);

    /**
     * Deletes a cache entry
     * @param key The cache key
     * @return A promise if it succeeded or not
     */
    Promise<Boolean> delete(String key);
}
