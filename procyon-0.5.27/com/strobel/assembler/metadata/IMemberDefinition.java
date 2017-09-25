package com.strobel.assembler.metadata;

public interface IMemberDefinition
{
    String getName();
    
    String getFullName();
    
    boolean isSpecialName();
    
    TypeReference getDeclaringType();
    
    long getFlags();
    
    int getModifiers();
    
    boolean isFinal();
    
    boolean isNonPublic();
    
    boolean isPrivate();
    
    boolean isProtected();
    
    boolean isPublic();
    
    boolean isStatic();
    
    boolean isSynthetic();
    
    boolean isDeprecated();
    
    boolean isPackagePrivate();
    
    String getBriefDescription();
    
    String getDescription();
    
    String getErasedDescription();
    
    String getSimpleDescription();
}
