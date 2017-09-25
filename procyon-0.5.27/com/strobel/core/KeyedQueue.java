package com.strobel.core;

import java.util.*;

public final class KeyedQueue<K, V>
{
    private final Map<K, Queue<V>> _data;
    
    public KeyedQueue() {
        super();
        this._data = new HashMap<K, Queue<V>>();
    }
    
    private Queue<V> getQueue(final K key) {
        Queue<V> queue = this._data.get(key);
        if (queue == null) {
            this._data.put(key, queue = new ArrayDeque<V>());
        }
        return queue;
    }
    
    public boolean add(final K key, final V value) {
        return this.getQueue(key).add(value);
    }
    
    public boolean offer(final K key, final V value) {
        return this.getQueue(key).offer(value);
    }
    
    public V poll(final K key) {
        return this.getQueue(key).poll();
    }
    
    public V peek(final K key) {
        return this.getQueue(key).peek();
    }
    
    public int size(final K key) {
        final Queue<V> queue = this._data.get(key);
        return (queue != null) ? queue.size() : 0;
    }
    
    public void clear() {
        this._data.clear();
    }
}
