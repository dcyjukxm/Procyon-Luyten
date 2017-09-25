package com.strobel.core;

public final class ByteBox implements IStrongBox
{
    public byte value;
    
    public ByteBox() {
        super();
    }
    
    public ByteBox(final byte value) {
        super();
        this.value = value;
    }
    
    @Override
    public Byte get() {
        return this.value;
    }
    
    @Override
    public void set(final Object value) {
        this.value = (byte)value;
    }
}
