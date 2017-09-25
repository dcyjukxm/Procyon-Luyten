package com.strobel.assembler.metadata.signatures;

import java.util.*;
import com.strobel.assembler.metadata.*;

public interface MetadataFactory
{
    GenericParameter makeTypeVariable(String param_0, FieldTypeSignature[] param_1);
    
    TypeReference makeParameterizedType(TypeReference param_0, TypeReference param_1, TypeReference... param_2);
    
    GenericParameter findTypeVariable(String param_0);
    
    WildcardType makeWildcard(FieldTypeSignature param_0, FieldTypeSignature param_1);
    
    TypeReference makeNamedType(String param_0);
    
    TypeReference makeArrayType(TypeReference param_0);
    
    TypeReference makeByte();
    
    TypeReference makeBoolean();
    
    TypeReference makeShort();
    
    TypeReference makeChar();
    
    TypeReference makeInt();
    
    TypeReference makeLong();
    
    TypeReference makeFloat();
    
    TypeReference makeDouble();
    
    TypeReference makeVoid();
    
    IMethodSignature makeMethodSignature(TypeReference param_0, List<TypeReference> param_1, List<GenericParameter> param_2, List<TypeReference> param_3);
    
    IClassSignature makeClassSignature(TypeReference param_0, List<TypeReference> param_1, List<GenericParameter> param_2);
}
