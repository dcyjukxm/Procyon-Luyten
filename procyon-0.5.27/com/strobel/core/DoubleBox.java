package com.strobel.core;

public final class DoubleBox implements IStrongBox
{
    public double value;
    
    public DoubleBox() {
        super();
    }
    
    public DoubleBox(final double value) {
        super();
        this.value = value;
    }
    
    @Override
    public Double get() {
        return this.value;
    }
    
    @Override
    public void set(final Object value) {
        this.value = (double)value;
    }
}
