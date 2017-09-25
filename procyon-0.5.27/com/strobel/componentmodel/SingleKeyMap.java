package com.strobel.componentmodel;

import com.strobel.annotations.*;
import com.strobel.core.*;

final class SingleKeyMap<V> implements FrugalKeyMap
{
    private final int _keyIndex;
    private final V _value;
    
    SingleKeyMap(final int keyIndex, final V value) {
        super();
        this._keyIndex = keyIndex;
        this._value = value;
    }
    
    @NotNull
    @Override
    public final <V> FrugalKeyMap plus(@NotNull final Key<V> key, @NotNull final V value) {
        VerifyArgument.notNull(key, "key");
        VerifyArgument.notNull(value, "value");
        if (key.hashCode() == this._keyIndex) {
            return new SingleKeyMap<Object>(key.hashCode(), value);
        }
        return new PairKeyMap(this._keyIndex, this._value, key.hashCode(), value);
    }
    
    @NotNull
    @Override
    public final <V> FrugalKeyMap minus(@NotNull final Key<V> key) {
        VerifyArgument.notNull(key, "key");
        if (key.hashCode() == this._keyIndex) {
            return SingleKeyMap.EMPTY;
        }
        return this;
    }
    
    @Override
    public final <V> V get(@NotNull final Key<V> key) {
        VerifyArgument.notNull(key, "key");
        if (key.hashCode() == this._keyIndex) {
            return (V)this._value;
        }
        return null;
    }
    
    @Override
    public final boolean isEmpty() {
        return false;
    }
}
