package com.strobel.assembler.metadata;

import java.util.*;

public interface IClassSignature extends IGenericParameterProvider
{
    TypeReference getBaseType();
    
    List<TypeReference> getExplicitInterfaces();
    
    boolean hasGenericParameters();
    
    List<GenericParameter> getGenericParameters();
}
