package com.strobel.assembler.metadata;

public interface IMetadataScope
{
    TypeReference lookupType(int param_0);
    
    FieldReference lookupField(int param_0);
    
    MethodReference lookupMethod(int param_0);
    
    MethodHandle lookupMethodHandle(int param_0);
    
    IMethodSignature lookupMethodType(int param_0);
    
    DynamicCallSite lookupDynamicCallSite(int param_0);
    
    FieldReference lookupField(int param_0, int param_1);
    
    MethodReference lookupMethod(int param_0, int param_1);
    
     <T> T lookupConstant(int param_0);
    
    Object lookup(int param_0);
}
