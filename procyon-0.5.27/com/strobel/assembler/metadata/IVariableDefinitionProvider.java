package com.strobel.assembler.metadata;

import java.util.*;

public interface IVariableDefinitionProvider
{
    boolean hasVariables();
    
    List<VariableDefinition> getVariables();
}
