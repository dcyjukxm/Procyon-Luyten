package com.strobel.assembler.metadata;

import java.util.*;

public interface IMethodSignature extends IGenericParameterProvider, IGenericContext
{
    boolean hasParameters();
    
    List<ParameterDefinition> getParameters();
    
    TypeReference getReturnType();
    
    List<TypeReference> getThrownTypes();
}
