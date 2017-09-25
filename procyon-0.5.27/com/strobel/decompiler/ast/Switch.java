package com.strobel.decompiler.ast;

import com.strobel.assembler.*;
import com.strobel.core.*;
import java.util.*;
import com.strobel.decompiler.*;

public final class Switch extends Node
{
    private final List<CaseBlock> _caseBlocks;
    private Expression _condition;
    
    public Switch() {
        super();
        this._caseBlocks = new Collection<CaseBlock>();
    }
    
    public final Expression getCondition() {
        return this._condition;
    }
    
    public final void setCondition(final Expression condition) {
        this._condition = condition;
    }
    
    public final List<CaseBlock> getCaseBlocks() {
        return this._caseBlocks;
    }
    
    @Override
    public final List<Node> getChildren() {
        final int size = this._caseBlocks.size() + ((this._condition != null) ? 1 : 0);
        final Node[] children = new Node[size];
        int i = 0;
        if (this._condition != null) {
            children[i++] = this._condition;
        }
        for (final CaseBlock caseBlock : this._caseBlocks) {
            children[i++] = caseBlock;
        }
        return ArrayUtilities.asUnmodifiableList(children);
    }
    
    @Override
    public final void writeTo(final ITextOutput output) {
        output.writeKeyword("switch");
        output.write(" (");
        if (this._condition != null) {
            this._condition.writeTo(output);
        }
        else {
            output.write("...");
        }
        output.writeLine(") {");
        output.indent();
        for (int i = 0, n = this._caseBlocks.size(); i < n; ++i) {
            final CaseBlock caseBlock = this._caseBlocks.get(i);
            if (i != 0) {
                output.writeLine();
            }
            caseBlock.writeTo(output);
        }
        output.unindent();
        output.writeLine("}");
    }
}
