package com.strobel.componentmodel;

import com.strobel.annotations.*;
import com.strobel.core.*;

final class EmptyKeyMap implements FrugalKeyMap
{
    @NotNull
    @Override
    public <V> FrugalKeyMap plus(@NotNull final Key<V> key, @NotNull final V value) {
        VerifyArgument.notNull(key, "key");
        VerifyArgument.notNull(value, "value");
        return new SingleKeyMap<Object>(key.hashCode(), value);
    }
    
    @NotNull
    @Override
    public final <V> FrugalKeyMap minus(@NotNull final Key<V> key) {
        VerifyArgument.notNull(key, "key");
        return this;
    }
    
    @Override
    public final <V> V get(@NotNull final Key<V> key) {
        return null;
    }
    
    @Override
    public final boolean isEmpty() {
        return true;
    }
}
