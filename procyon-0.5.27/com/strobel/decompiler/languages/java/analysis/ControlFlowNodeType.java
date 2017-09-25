package com.strobel.decompiler.languages.java.analysis;

public enum ControlFlowNodeType
{
    None("None", 0), 
    StartNode("StartNode", 1), 
    BetweenStatements("BetweenStatements", 2), 
    EndNode("EndNode", 3), 
    LoopCondition("LoopCondition", 4);
}
