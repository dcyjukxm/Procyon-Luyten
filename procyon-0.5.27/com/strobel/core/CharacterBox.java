package com.strobel.core;

public final class CharacterBox implements IStrongBox
{
    public char value;
    
    public CharacterBox() {
        super();
    }
    
    public CharacterBox(final char value) {
        super();
        this.value = value;
    }
    
    @Override
    public Character get() {
        return this.value;
    }
    
    @Override
    public void set(final Object value) {
        this.value = (char)value;
    }
}
