package com.strobel.collections.concurrent;

import com.strobel.annotations.*;

public interface IntObjectEntry<V>
{
    int key();
    
    @NotNull
    V value();
}
