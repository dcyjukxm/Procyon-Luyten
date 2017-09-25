package com.strobel.core;

import com.strobel.functions.*;

public final class MutableInteger
{
    public static final Supplier<MutableInteger> SUPPLIER;
    private int _value;
    
    static {
        SUPPLIER = new Supplier<MutableInteger>() {
            @Override
            public MutableInteger get() {
                return new MutableInteger();
            }
        };
    }
    
    public MutableInteger() {
        super();
    }
    
    public MutableInteger(final int value) {
        super();
        this._value = value;
    }
    
    public int getValue() {
        return this._value;
    }
    
    public void setValue(final int value) {
        this._value = value;
    }
    
    public MutableInteger increment() {
        ++this._value;
        return this;
    }
    
    public MutableInteger decrement() {
        --this._value;
        return this;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final MutableInteger that = (MutableInteger)o;
        return this._value == that._value;
    }
    
    @Override
    public int hashCode() {
        return this._value;
    }
}
