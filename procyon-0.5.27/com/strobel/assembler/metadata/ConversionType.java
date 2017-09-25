package com.strobel.assembler.metadata;

public enum ConversionType
{
    IDENTITY("IDENTITY", 0), 
    IMPLICIT("IMPLICIT", 1), 
    EXPLICIT("EXPLICIT", 2), 
    EXPLICIT_TO_UNBOXED("EXPLICIT_TO_UNBOXED", 3), 
    NONE("NONE", 4);
}
