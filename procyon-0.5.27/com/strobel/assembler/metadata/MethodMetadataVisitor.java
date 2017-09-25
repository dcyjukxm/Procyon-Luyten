package com.strobel.assembler.metadata;

public interface MethodMetadataVisitor<P, R>
{
    R visitParameterizedMethod(MethodReference param_0, P param_1);
    
    R visitMethod(MethodReference param_0, P param_1);
}
