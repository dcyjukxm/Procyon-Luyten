package com.strobel.assembler.ir.attributes;

import com.strobel.assembler.metadata.*;

public final class MethodParameterEntry
{
    private final String _name;
    private final int _flags;
    
    public MethodParameterEntry(final String name, final int flags) {
        super();
        this._name = name;
        this._flags = flags;
    }
    
    public String getName() {
        return this._name;
    }
    
    public int getFlags() {
        return this._flags;
    }
    
    @Override
    public String toString() {
        return "MethodParameterEntry{name='" + this._name + "'" + ", flags=" + Flags.toString(this._flags) + '}';
    }
}
