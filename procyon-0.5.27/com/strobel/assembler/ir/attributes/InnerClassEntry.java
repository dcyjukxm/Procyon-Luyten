package com.strobel.assembler.ir.attributes;

import com.strobel.assembler.metadata.*;

public final class InnerClassEntry
{
    private final String _innerClassName;
    private final String _outerClassName;
    private final String _shortName;
    private final int _accessFlags;
    
    public InnerClassEntry(final String innerClassName, final String outerClassName, final String shortName, final int accessFlags) {
        super();
        this._innerClassName = innerClassName;
        this._outerClassName = outerClassName;
        this._shortName = shortName;
        this._accessFlags = accessFlags;
    }
    
    public String getInnerClassName() {
        return this._innerClassName;
    }
    
    public String getOuterClassName() {
        return this._outerClassName;
    }
    
    public String getShortName() {
        return this._shortName;
    }
    
    public int getAccessFlags() {
        return this._accessFlags;
    }
    
    @Override
    public String toString() {
        return "InnerClassEntry{InnerClassName='" + this._innerClassName + '\'' + ", OuterClassName='" + this._outerClassName + '\'' + ", ShortName='" + this._shortName + '\'' + ", AccessFlags=[" + Flags.toString(this._accessFlags) + "]}";
    }
}
