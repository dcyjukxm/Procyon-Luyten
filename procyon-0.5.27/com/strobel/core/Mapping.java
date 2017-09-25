package com.strobel.core;

public abstract class Mapping<T>
{
    private final String _name;
    
    protected Mapping() {
        this(null);
    }
    
    protected Mapping(final String name) {
        super();
        this._name = name;
    }
    
    public abstract T apply(final T param_0);
    
    @Override
    public String toString() {
        if (this._name != null) {
            return this._name;
        }
        return super.toString();
    }
}
