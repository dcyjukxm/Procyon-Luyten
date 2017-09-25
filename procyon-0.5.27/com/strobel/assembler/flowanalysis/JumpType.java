package com.strobel.assembler.flowanalysis;

public enum JumpType
{
    Normal("Normal", 0), 
    JumpToExceptionHandler("JumpToExceptionHandler", 1), 
    LeaveTry("LeaveTry", 2), 
    EndFinally("EndFinally", 3);
}
