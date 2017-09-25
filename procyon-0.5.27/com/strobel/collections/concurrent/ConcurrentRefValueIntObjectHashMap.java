package com.strobel.collections.concurrent;

import java.lang.ref.*;
import com.strobel.annotations.*;
import java.util.*;
import com.strobel.util.*;

abstract class ConcurrentRefValueIntObjectHashMap<V> implements ConcurrentIntObjectMap<V>
{
    private final ConcurrentIntObjectHashMap<IntReference<V>> _map;
    private final ReferenceQueue<V> _queue;
    
    ConcurrentRefValueIntObjectHashMap() {
        super();
        this._map = new ConcurrentIntObjectHashMap<IntReference<V>>();
        this._queue = new ReferenceQueue<V>();
    }
    
    protected abstract IntReference<V> createReference(final int param_0, @NotNull final V param_1, final ReferenceQueue<V> param_2);
    
    private void processQueue() {
        while (true) {
            final IntReference<V> reference = (IntReference)this._queue.poll();
            if (reference == null) {
                break;
            }
            this._map.remove(reference.key(), reference);
        }
    }
    
    @NotNull
    @Override
    public V addOrGet(final int key, @NotNull final V value) {
        this.processQueue();
        final IntReference<V> newReference = this.createReference(key, value, this._queue);
        boolean replaced;
        do {
            final IntReference<V> oldReference = this._map.putIfAbsent(key, newReference);
            if (oldReference == null) {
                return value;
            }
            final V oldValue = oldReference.get();
            if (oldValue != null) {
                return oldValue;
            }
            replaced = this._map.replace(key, oldReference, newReference);
        } while (!replaced);
        return value;
    }
    
    @Override
    public V putIfAbsent(final int key, @NotNull final V value) {
        this.processQueue();
        final IntReference<V> newReference = this.createReference(key, value, this._queue);
        boolean replaced;
        do {
            final IntReference<V> oldReference = this._map.putIfAbsent(key, newReference);
            if (oldReference == null) {
                return null;
            }
            final V oldValue = oldReference.get();
            if (oldValue != null) {
                return oldValue;
            }
            replaced = this._map.replace(key, oldReference, newReference);
        } while (!replaced);
        return null;
    }
    
    @Override
    public boolean remove(final int key, @NotNull final V value) {
        this.processQueue();
        return this._map.remove(key, this.createReference(key, value, this._queue));
    }
    
    @Override
    public boolean replace(final int key, @NotNull final V oldValue, @NotNull final V newValue) {
        this.processQueue();
        return this._map.replace(key, this.createReference(key, oldValue, this._queue), this.createReference(key, newValue, this._queue));
    }
    
    @Override
    public V put(final int key, @NotNull final V value) {
        this.processQueue();
        final IntReference<V> oldReference = this._map.put(key, this.createReference(key, value, this._queue));
        return (oldReference != null) ? oldReference.get() : null;
    }
    
    @Override
    public V get(final int key) {
        final IntReference<V> reference = this._map.get(key);
        return (reference != null) ? reference.get() : null;
    }
    
    @Override
    public V remove(final int key) {
        this.processQueue();
        final IntReference<V> reference = this._map.remove(key);
        return (reference != null) ? reference.get() : null;
    }
    
    @Override
    public int size() {
        return this._map.size();
    }
    
    @Override
    public boolean isEmpty() {
        return this._map.isEmpty();
    }
    
    @Override
    public boolean contains(final int key) {
        return this._map.contains(key);
    }
    
    @Override
    public void clear() {
        this._map.clear();
        this.processQueue();
    }
    
    @NotNull
    @Override
    public int[] keys() {
        return this._map.keys();
    }
    
    @NotNull
    @Override
    public Iterable<IntObjectEntry<V>> entries() {
        return new Iterable<IntObjectEntry<V>>() {
            final /* synthetic */ ConcurrentRefValueIntObjectHashMap this$0;
            
            @Override
            public Iterator<IntObjectEntry<V>> iterator() {
                return new Iterator<IntObjectEntry<V>>() {
                    final Iterator<IntObjectEntry<IntReference<V>>> entryIterator = ConcurrentRefValueIntObjectHashMap.access$0(ConcurrentRefValueIntObjectHashMap$1.access$0(Iterable.this)).entries().iterator();
                    IntObjectEntry<V> next = this.nextLiveEntry();
                    
                    @Override
                    public boolean hasNext() {
                        return this.next != null;
                    }
                    
                    @Override
                    public IntObjectEntry<V> next() {
                        if (!this.hasNext()) {
                            throw new NoSuchElementException();
                        }
                        final IntObjectEntry<V> result = this.next;
                        this.next = this.nextLiveEntry();
                        return result;
                    }
                    
                    @Override
                    public void remove() {
                        throw ContractUtils.unsupported();
                    }
                    
                    private IntObjectEntry<V> nextLiveEntry() {
                        while (this.entryIterator.hasNext()) {
                            final IntObjectEntry<IntReference<V>> entry = this.entryIterator.next();
                            final V value = entry.value().get();
                            if (value == null) {
                                continue;
                            }
                            final int key = entry.key();
                            return new IntObjectEntry<V>() {
                                @Override
                                public int key() {
                                    return key;
                                }
                                
                                @NotNull
                                @Override
                                public V value() {
                                    return value;
                                }
                            };
                        }
                        return null;
                    }
                };
            }
            
            static /* synthetic */ ConcurrentRefValueIntObjectHashMap access$0(final ConcurrentRefValueIntObjectHashMap$1 param_0) {
                return param_0.this$0;
            }
        };
    }
    
    static /* synthetic */ ConcurrentIntObjectHashMap access$0(final ConcurrentRefValueIntObjectHashMap param_0) {
        return param_0._map;
    }
    
    protected interface IntReference<V>
    {
        int key();
        
        V get();
    }
}
