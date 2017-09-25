package com.strobel.decompiler.languages;

public enum EntityType
{
    NONE("NONE", 0), 
    TYPE_DEFINITION("TYPE_DEFINITION", 1), 
    ENUM_VALUE("ENUM_VALUE", 2), 
    FIELD("FIELD", 3), 
    METHOD("METHOD", 4), 
    CONSTRUCTOR("CONSTRUCTOR", 5), 
    PARAMETER("PARAMETER", 6);
}
