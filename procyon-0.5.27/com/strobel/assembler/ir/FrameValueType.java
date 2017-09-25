package com.strobel.assembler.ir;

public enum FrameValueType
{
    Empty("Empty", 0), 
    Top("Top", 1), 
    Integer("Integer", 2), 
    Float("Float", 3), 
    Long("Long", 4), 
    Double("Double", 5), 
    Null("Null", 6), 
    UninitializedThis("UninitializedThis", 7), 
    Reference("Reference", 8), 
    Uninitialized("Uninitialized", 9), 
    Address("Address", 10);
    
    public final boolean isDoubleWord() {
        return this == FrameValueType.Double || this == FrameValueType.Long;
    }
}
