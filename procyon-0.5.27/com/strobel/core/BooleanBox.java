package com.strobel.core;

public final class BooleanBox implements IStrongBox
{
    public boolean value;
    
    public BooleanBox() {
        super();
    }
    
    public BooleanBox(final boolean value) {
        super();
        this.value = value;
    }
    
    @Override
    public Boolean get() {
        return this.value;
    }
    
    @Override
    public void set(final Object value) {
        this.value = (boolean)value;
    }
}
