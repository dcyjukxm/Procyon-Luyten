package com.strobel.assembler.metadata;

public interface FieldMetadataVisitor<P, R>
{
    R visitField(FieldReference param_0, P param_1);
}
