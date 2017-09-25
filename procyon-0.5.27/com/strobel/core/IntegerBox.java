package com.strobel.core;

public final class IntegerBox implements IStrongBox
{
    public int value;
    
    public IntegerBox() {
        super();
    }
    
    public IntegerBox(final int value) {
        super();
        this.value = value;
    }
    
    @Override
    public Integer get() {
        return this.value;
    }
    
    @Override
    public void set(final Object value) {
        this.value = (int)value;
    }
}
