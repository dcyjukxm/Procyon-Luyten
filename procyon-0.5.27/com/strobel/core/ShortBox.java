package com.strobel.core;

public final class ShortBox implements IStrongBox
{
    public short value;
    
    public ShortBox() {
        super();
    }
    
    public ShortBox(final short value) {
        super();
        this.value = value;
    }
    
    @Override
    public Short get() {
        return this.value;
    }
    
    @Override
    public void set(final Object value) {
        this.value = (short)value;
    }
}
