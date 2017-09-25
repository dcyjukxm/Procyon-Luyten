package com.strobel.decompiler.languages.java.ast;

public enum SimpleNameLookupMode
{
    EXPRESSION("EXPRESSION", 0), 
    INVOCATION_TARGET("INVOCATION_TARGET", 1), 
    TYPE("TYPE", 2), 
    TYPE_IN_IMPORT_DECLARATION("TYPE_IN_IMPORT_DECLARATION", 3), 
    BASE_TYPE_REFERENCE("BASE_TYPE_REFERENCE", 4);
}
