package com.strobel.assembler.metadata;

public interface IConstantValueProvider
{
    boolean hasConstantValue();
    
    Object getConstantValue();
}
