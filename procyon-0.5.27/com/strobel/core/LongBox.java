package com.strobel.core;

public final class LongBox implements IStrongBox
{
    public long value;
    
    public LongBox() {
        super();
    }
    
    public LongBox(final long value) {
        super();
        this.value = value;
    }
    
    @Override
    public Long get() {
        return this.value;
    }
    
    @Override
    public void set(final Object value) {
        this.value = (long)value;
    }
}
