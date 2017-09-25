package com.strobel.assembler.flowanalysis;

public enum ControlFlowNodeType
{
    Normal("Normal", 0), 
    EntryPoint("EntryPoint", 1), 
    RegularExit("RegularExit", 2), 
    ExceptionalExit("ExceptionalExit", 3), 
    CatchHandler("CatchHandler", 4), 
    FinallyHandler("FinallyHandler", 5), 
    EndFinally("EndFinally", 6);
}
