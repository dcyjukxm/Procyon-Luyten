package com.strobel.componentmodel;

import java.util.concurrent.atomic.*;
import com.strobel.collections.concurrent.*;
import com.strobel.core.*;
import com.strobel.annotations.*;
import java.util.*;

public class Key<T>
{
    private static final AtomicInteger _keyCounter;
    private static final ConcurrentWeakIntObjectHashMap<Key<?>> _allKeys;
    private final int _index;
    @NotNull
    private final String _name;
    
    static {
        _keyCounter = new AtomicInteger();
        _allKeys = new ConcurrentWeakIntObjectHashMap<Key<?>>();
    }
    
    public static <T> Key<T> getKeyByIndex(final int index) {
        return (Key)Key._allKeys.get(index);
    }
    
    public static <T> Key<T> create(@NotNull final String name) {
        return new Key<T>(name);
    }
    
    public Key(@NotNull final String name) {
        super();
        this._index = Key._keyCounter.getAndIncrement();
        this._name = VerifyArgument.notNull(name, "name");
    }
    
    @Override
    public final int hashCode() {
        return this._index;
    }
    
    @Override
    public final boolean equals(final Object obj) {
        return obj == this;
    }
    
    @Override
    public String toString() {
        return "Key(" + this._name + ")";
    }
    
    @Nullable
    public T get(@Nullable final UserDataStore store) {
        return (store == null) ? null : store.getUserData(this);
    }
    
    @Nullable
    public T get(@Nullable final Map<Key<?>, ?> store) {
        return (T)((store == null) ? null : store.get(this));
    }
    
    @Nullable
    public T get(@Nullable final UserDataStore store, @Nullable final T defaultValue) {
        final T value = this.get(store);
        return (value != null) ? value : defaultValue;
    }
    
    @Nullable
    public T get(@Nullable final Map<Key<?>, ?> store, @Nullable final T defaultValue) {
        final T value = this.get(store);
        return (value != null) ? value : defaultValue;
    }
    
    public boolean isPresent(@Nullable final UserDataStore store) {
        return this.get(store) != null;
    }
    
    public void set(@Nullable final UserDataStore store, @Nullable final T value) {
        if (store != null) {
            store.putUserData(this, value);
        }
    }
    
    public void set(@Nullable final Map<Key<?>, Object> store, @Nullable final T value) {
        if (store != null) {
            store.put(this, value);
        }
    }
}
