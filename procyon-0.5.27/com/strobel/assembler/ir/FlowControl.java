package com.strobel.assembler.ir;

public enum FlowControl
{
    Branch("Branch", 0), 
    Breakpoint("Breakpoint", 1), 
    Call("Call", 2), 
    ConditionalBranch("ConditionalBranch", 3), 
    Next("Next", 4), 
    Return("Return", 5), 
    Throw("Throw", 6);
}
