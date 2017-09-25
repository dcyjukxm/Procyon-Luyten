package com.strobel.decompiler.languages.java.ast;

public enum NodeType
{
    UNKNOWN("UNKNOWN", 0), 
    TYPE_REFERENCE("TYPE_REFERENCE", 1), 
    TYPE_DECLARATION("TYPE_DECLARATION", 2), 
    MEMBER("MEMBER", 3), 
    STATEMENT("STATEMENT", 4), 
    EXPRESSION("EXPRESSION", 5), 
    TOKEN("TOKEN", 6), 
    WHITESPACE("WHITESPACE", 7), 
    PATTERN("PATTERN", 8);
}
