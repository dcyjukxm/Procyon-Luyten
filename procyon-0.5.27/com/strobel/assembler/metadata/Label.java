package com.strobel.assembler.metadata;

public final class Label
{
    int index;
    
    public Label(final int label) {
        super();
        this.index = label;
    }
    
    public int getIndex() {
        return this.index;
    }
    
    public void setIndex(final int index) {
        this.index = index;
    }
    
    @Override
    public int hashCode() {
        return this.index;
    }
    
    @Override
    public boolean equals(final Object o) {
        return o instanceof Label && this.equals((Label)o);
    }
    
    public boolean equals(final Label other) {
        return other != null && other.index == this.index;
    }
}
