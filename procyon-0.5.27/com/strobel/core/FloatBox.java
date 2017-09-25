package com.strobel.core;

public final class FloatBox implements IStrongBox
{
    public float value;
    
    public FloatBox() {
        super();
    }
    
    public FloatBox(final float value) {
        super();
        this.value = value;
    }
    
    @Override
    public Float get() {
        return this.value;
    }
    
    @Override
    public void set(final Object value) {
        this.value = (float)value;
    }
}
