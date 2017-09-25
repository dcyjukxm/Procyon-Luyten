package com.strobel.core;

import com.strobel.functions.*;

public final class StrongBox<T> implements IStrongBox, Block<T>
{
    public T value;
    
    public StrongBox() {
        super();
    }
    
    public StrongBox(final T value) {
        super();
        this.value = value;
    }
    
    @Override
    public T get() {
        return this.value;
    }
    
    @Override
    public void set(final Object value) {
        this.value = (T)value;
    }
    
    @Override
    public void accept(final T input) {
        this.value = input;
    }
    
    @Override
    public String toString() {
        return "StrongBox{value=" + this.value + '}';
    }
}
