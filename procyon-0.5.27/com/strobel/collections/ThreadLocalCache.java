package com.strobel.collections;

final class ThreadLocalCache<K, V> extends Cache<K, V>
{
    private final Cache<K, V> _parent;
    private final ThreadLocal<SatelliteCache<K, V>> _threadCaches;
    
    public ThreadLocalCache() {
        super();
        this._threadCaches = new ThreadLocal<SatelliteCache<K, V>>() {
            @Override
            protected SatelliteCache<K, V> initialValue() {
                return new SatelliteCache<K, V>(ThreadLocalCache.access$0(ThreadLocalCache.this));
            }
        };
        this._parent = null;
    }
    
    @Override
    public Cache<K, V> getSatelliteCache() {
        return this._threadCaches.get();
    }
    
    @Override
    public boolean replace(final K key, final V expectedValue, final V updatedValue) {
        return this._threadCaches.get().replace(key, expectedValue, updatedValue);
    }
    
    public ThreadLocalCache(final Cache<K, V> parent) {
        super();
        this._threadCaches = new ThreadLocal<SatelliteCache<K, V>>() {
            @Override
            protected SatelliteCache<K, V> initialValue() {
                return new SatelliteCache<K, V>(ThreadLocalCache.access$0(ThreadLocalCache.this));
            }
        };
        this._parent = parent;
    }
    
    @Override
    public V cache(final K key, final V value) {
        return this._threadCaches.get().cache(key, value);
    }
    
    @Override
    public V get(final K key) {
        return this._threadCaches.get().get(key);
    }
    
    static /* synthetic */ Cache access$0(final ThreadLocalCache param_0) {
        return param_0._parent;
    }
}
