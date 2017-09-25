package com.strobel.assembler.metadata;

import java.util.*;

public interface IGenericParameterProvider
{
    boolean hasGenericParameters();
    
    boolean isGenericDefinition();
    
    List<GenericParameter> getGenericParameters();
}
