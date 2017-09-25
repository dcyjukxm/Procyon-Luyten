package com.strobel.collections;

import java.util.concurrent.*;

final class TopLevelCache<K, V> extends Cache<K, V>
{
    private final ConcurrentHashMap<K, V> _cache;
    
    TopLevelCache() {
        super();
        this._cache = new ConcurrentHashMap<K, V>();
    }
    
    @Override
    public V cache(final K key, final V value) {
        final V cachedValue = this._cache.putIfAbsent(key, value);
        return (cachedValue != null) ? cachedValue : value;
    }
    
    @Override
    public Cache<K, V> getSatelliteCache() {
        return Cache.createSatelliteCache((Cache<K, V>)this);
    }
    
    @Override
    public boolean replace(final K key, final V expectedValue, final V updatedValue) {
        if (expectedValue == null) {
            return this._cache.putIfAbsent(key, updatedValue) == null;
        }
        return this._cache.replace(key, expectedValue, updatedValue);
    }
    
    @Override
    public V get(final K key) {
        return this._cache.get(key);
    }
}
