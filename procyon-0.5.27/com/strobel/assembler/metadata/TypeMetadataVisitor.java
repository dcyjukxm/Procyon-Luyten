package com.strobel.assembler.metadata;

public interface TypeMetadataVisitor<P, R>
{
    R visitType(TypeReference param_0, P param_1);
    
    R visitArrayType(ArrayType param_0, P param_1);
    
    R visitGenericParameter(GenericParameter param_0, P param_1);
    
    R visitWildcard(WildcardType param_0, P param_1);
    
    R visitCapturedType(CapturedType param_0, P param_1);
    
    R visitCompoundType(CompoundTypeReference param_0, P param_1);
    
    R visitParameterizedType(TypeReference param_0, P param_1);
    
    R visitPrimitiveType(PrimitiveType param_0, P param_1);
    
    R visitClassType(TypeReference param_0, P param_1);
    
    R visitNullType(TypeReference param_0, P param_1);
    
    R visitBottomType(TypeReference param_0, P param_1);
    
    R visitRawType(RawType param_0, P param_1);
}
