package com.strobel.decompiler.languages.java.ast;

public enum UnaryOperatorType
{
    ANY("ANY", 0), 
    NOT("NOT", 1), 
    BITWISE_NOT("BITWISE_NOT", 2), 
    MINUS("MINUS", 3), 
    PLUS("PLUS", 4), 
    INCREMENT("INCREMENT", 5), 
    DECREMENT("DECREMENT", 6), 
    POST_INCREMENT("POST_INCREMENT", 7), 
    POST_DECREMENT("POST_DECREMENT", 8);
}
