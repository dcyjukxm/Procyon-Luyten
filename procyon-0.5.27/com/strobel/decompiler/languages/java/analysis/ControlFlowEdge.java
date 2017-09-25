package com.strobel.decompiler.languages.java.analysis;

import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.core.*;
import java.util.*;

public class ControlFlowEdge
{
    private final ControlFlowNode _from;
    private final ControlFlowNode _to;
    private final ControlFlowEdgeType _type;
    List<TryCatchStatement> jumpOutOfTryFinally;
    
    public ControlFlowEdge(final ControlFlowNode from, final ControlFlowNode to, final ControlFlowEdgeType type) {
        super();
        this._from = VerifyArgument.notNull(from, "from");
        this._to = VerifyArgument.notNull(to, "to");
        this._type = type;
    }
    
    final void AddJumpOutOfTryFinally(final TryCatchStatement tryFinally) {
        if (this.jumpOutOfTryFinally == null) {
            this.jumpOutOfTryFinally = new ArrayList<TryCatchStatement>();
        }
        this.jumpOutOfTryFinally.add(tryFinally);
    }
    
    public final boolean isLeavingTryFinally() {
        return this.jumpOutOfTryFinally != null;
    }
    
    public final Iterable<TryCatchStatement> getTryFinallyStatements() {
        if (this.jumpOutOfTryFinally != null) {
            return this.jumpOutOfTryFinally;
        }
        return (Iterable<TryCatchStatement>)Collections.emptyList();
    }
    
    public final ControlFlowNode getFrom() {
        return this._from;
    }
    
    public final ControlFlowNode getTo() {
        return this._to;
    }
    
    public final ControlFlowEdgeType getType() {
        return this._type;
    }
}
