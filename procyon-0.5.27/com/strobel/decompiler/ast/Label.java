package com.strobel.decompiler.ast;

import com.strobel.decompiler.*;

public class Label extends Node
{
    private String _name;
    private int _offset;
    
    public Label() {
        super();
        this._offset = -1;
    }
    
    public Label(final String name) {
        super();
        this._offset = -1;
        this._name = name;
    }
    
    public String getName() {
        return this._name;
    }
    
    public void setName(final String name) {
        this._name = name;
    }
    
    public int getOffset() {
        return this._offset;
    }
    
    public void setOffset(final int offset) {
        this._offset = offset;
    }
    
    @Override
    public void writeTo(final ITextOutput output) {
        output.writeDefinition(String.valueOf(this.getName()) + ":", this);
    }
}
