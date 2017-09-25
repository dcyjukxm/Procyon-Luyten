package com.strobel.decompiler.languages.java.analysis;

import com.strobel.decompiler.languages.java.ast.*;
import java.util.*;

public class ControlFlowNode
{
    private final Statement _previousStatement;
    private final Statement _nextStatement;
    private final ControlFlowNodeType _type;
    private final List<ControlFlowEdge> _outgoing;
    private final List<ControlFlowEdge> _incoming;
    
    public ControlFlowNode(final Statement previousStatement, final Statement nextStatement, final ControlFlowNodeType type) {
        super();
        this._outgoing = new ArrayList<ControlFlowEdge>();
        this._incoming = new ArrayList<ControlFlowEdge>();
        if (previousStatement == null && nextStatement == null) {
            throw new IllegalArgumentException("previousStatement and nextStatement must not be both null");
        }
        this._previousStatement = previousStatement;
        this._nextStatement = nextStatement;
        this._type = type;
    }
    
    public Statement getPreviousStatement() {
        return this._previousStatement;
    }
    
    public Statement getNextStatement() {
        return this._nextStatement;
    }
    
    public ControlFlowNodeType getType() {
        return this._type;
    }
    
    public List<ControlFlowEdge> getOutgoing() {
        return this._outgoing;
    }
    
    public List<ControlFlowEdge> getIncoming() {
        return this._incoming;
    }
}
