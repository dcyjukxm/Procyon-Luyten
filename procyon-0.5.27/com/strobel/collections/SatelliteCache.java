package com.strobel.collections;

import java.util.*;

final class SatelliteCache<K, V> extends Cache<K, V>
{
    private final Cache<K, V> _parent;
    private final HashMap<K, V> _cache;
    
    public SatelliteCache() {
        super();
        this._cache = new HashMap<K, V>();
        this._parent = null;
    }
    
    @Override
    public Cache<K, V> getSatelliteCache() {
        return this;
    }
    
    @Override
    public boolean replace(final K key, final V expectedValue, final V updatedValue) {
        if (this._parent != null && !this._parent.replace(key, expectedValue, updatedValue)) {
            return false;
        }
        this._cache.put(key, updatedValue);
        return true;
    }
    
    public SatelliteCache(final Cache<K, V> parent) {
        super();
        this._cache = new HashMap<K, V>();
        this._parent = parent;
    }
    
    @Override
    public V cache(final K key, final V value) {
        V cachedValue = this._cache.get(key);
        if (cachedValue != null) {
            return cachedValue;
        }
        if (this._parent != null) {
            cachedValue = this._parent.cache(key, value);
        }
        else {
            cachedValue = value;
        }
        this._cache.put(key, cachedValue);
        return cachedValue;
    }
    
    @Override
    public V get(final K key) {
        V cachedValue = this._cache.get(key);
        if (cachedValue != null) {
            return cachedValue;
        }
        if (this._parent != null) {
            cachedValue = this._parent.get(key);
            if (cachedValue != null) {
                this._cache.put(key, cachedValue);
            }
        }
        return cachedValue;
    }
}
