package com.strobel.collections;

import com.strobel.annotations.*;
import com.strobel.core.*;

public abstract class Cache<K, V>
{
    public boolean contains(final K key) {
        return this.get(key) != null;
    }
    
    public boolean contains(final K key, final V value) {
        final V cachedValue = this.get(key);
        return cachedValue != null && cachedValue.equals(value);
    }
    
    public abstract Cache<K, V> getSatelliteCache();
    
    public abstract boolean replace(final K param_0, @Nullable final V param_1, final V param_2);
    
    public abstract V get(final K param_0);
    
    public abstract V cache(final K param_0, final V param_1);
    
    public static <K, V> Cache<K, V> createTopLevelCache() {
        return new TopLevelCache<K, V>();
    }
    
    public static <K, V> Cache<K, V> createSatelliteCache() {
        return new SatelliteCache<K, V>();
    }
    
    public static <K, V> Cache<K, V> createSatelliteCache(final Cache<K, V> parent) {
        return new SatelliteCache<K, V>(VerifyArgument.notNull(parent, "parent"));
    }
    
    public static <K, V> Cache<K, V> createSatelliteIdentityCache() {
        return new SatelliteCache<K, V>();
    }
    
    public static <K, V> Cache<K, V> createSatelliteIdentityCache(final Cache<K, V> parent) {
        return new SatelliteCache<K, V>(VerifyArgument.notNull(parent, "parent"));
    }
    
    public static <K, V> Cache<K, V> createThreadLocalCache() {
        return new ThreadLocalCache<K, V>();
    }
    
    public static <K, V> Cache<K, V> createThreadLocalIdentityCache() {
        return new ThreadLocalCache<K, V>();
    }
    
    public static <K, V> Cache<K, V> createThreadLocalCache(final Cache<K, V> parent) {
        return new ThreadLocalCache<K, V>(VerifyArgument.notNull(parent, "parent"));
    }
    
    public static <K, V> Cache<K, V> createThreadLocalIdentityCache(final Cache<K, V> parent) {
        return new ThreadLocalIdentityCache<K, V>(VerifyArgument.notNull(parent, "parent"));
    }
}
