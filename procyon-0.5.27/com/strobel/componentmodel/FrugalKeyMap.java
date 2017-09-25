package com.strobel.componentmodel;

import com.strobel.annotations.*;

public interface FrugalKeyMap
{
    public static final FrugalKeyMap EMPTY = new EmptyKeyMap();
    
    @NotNull
     <V> FrugalKeyMap plus(@NotNull Key<V> param_0, @NotNull V param_1);
    
    @NotNull
     <V> FrugalKeyMap minus(@NotNull Key<V> param_0);
    
    @Nullable
     <V> V get(@NotNull Key<V> param_0);
    
    String toString();
    
    boolean isEmpty();
}
