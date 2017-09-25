package com.strobel.collections.concurrent;

import com.strobel.annotations.*;

public interface ConcurrentIntObjectMap<V>
{
    @NotNull
    V addOrGet(int param_0, @NotNull V param_1);
    
    boolean remove(int param_0, @NotNull V param_1);
    
    boolean replace(int param_0, @NotNull V param_1, @NotNull V param_2);
    
    @Nullable
    V put(int param_0, @NotNull V param_1);
    
    V putIfAbsent(int param_0, @NotNull V param_1);
    
    @Nullable
    V get(int param_0);
    
    @Nullable
    V remove(int param_0);
    
    int size();
    
    boolean isEmpty();
    
    boolean contains(int param_0);
    
    void clear();
    
    @NotNull
    int[] keys();
    
    @NotNull
    Iterable<IntObjectEntry<V>> entries();
}
