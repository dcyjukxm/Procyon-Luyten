package com.strobel.componentmodel;

import com.strobel.core.*;
import com.strobel.annotations.*;

final class PairKeyMap implements FrugalKeyMap
{
    private final int _keyIndex1;
    private final int _keyIndex2;
    private final Object _value1;
    private final Object _value2;
    
    PairKeyMap(final int keyIndex1, final Object value1, final int keyIndex2, final Object value2) {
        super();
        this._keyIndex1 = keyIndex1;
        this._keyIndex2 = keyIndex2;
        this._value1 = VerifyArgument.notNull(value1, "value1");
        this._value2 = VerifyArgument.notNull(value2, "value2");
    }
    
    @NotNull
    @Override
    public final <V> FrugalKeyMap plus(@NotNull final Key<V> key, @NotNull final V value) {
        VerifyArgument.notNull(key, "key");
        VerifyArgument.notNull(value, "value");
        final int keyIndex = key.hashCode();
        if (keyIndex == this._keyIndex1) {
            return new PairKeyMap(keyIndex, value, this._keyIndex2, this._value2);
        }
        if (keyIndex == this._keyIndex2) {
            return new PairKeyMap(keyIndex, value, this._keyIndex1, this._value1);
        }
        return new ArrayKeyMap(new int[] { this._keyIndex1, this._keyIndex2, keyIndex }, new Object[] { this._value1, this._value2, value });
    }
    
    @NotNull
    @Override
    public final <V> FrugalKeyMap minus(@NotNull final Key<V> key) {
        VerifyArgument.notNull(key, "key");
        final int keyIndex = key.hashCode();
        if (keyIndex == this._keyIndex1) {
            return new SingleKeyMap<Object>(this._keyIndex2, this._value2);
        }
        if (keyIndex == this._keyIndex2) {
            return new SingleKeyMap<Object>(this._keyIndex1, this._value1);
        }
        return this;
    }
    
    @Override
    public final <V> V get(@NotNull final Key<V> key) {
        VerifyArgument.notNull(key, "key");
        if (key.hashCode() == this._keyIndex1) {
            return (V)this._value1;
        }
        if (key.hashCode() == this._keyIndex2) {
            return (V)this._value2;
        }
        return null;
    }
    
    @Override
    public final boolean isEmpty() {
        return false;
    }
}
