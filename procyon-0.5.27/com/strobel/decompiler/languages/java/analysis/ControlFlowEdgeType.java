package com.strobel.decompiler.languages.java.analysis;

public enum ControlFlowEdgeType
{
    Normal("Normal", 0), 
    ConditionTrue("ConditionTrue", 1), 
    ConditionFalse("ConditionFalse", 2), 
    Jump("Jump", 3);
}
