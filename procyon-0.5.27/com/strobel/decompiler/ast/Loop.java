package com.strobel.decompiler.ast;

import java.util.*;
import com.strobel.core.*;
import com.strobel.decompiler.*;

public final class Loop extends Node
{
    private LoopType _loopType;
    private Expression _condition;
    private Block _body;
    
    public Loop() {
        super();
        this._loopType = LoopType.PreCondition;
    }
    
    public final Expression getCondition() {
        return this._condition;
    }
    
    public final void setCondition(final Expression condition) {
        this._condition = condition;
    }
    
    public final Block getBody() {
        return this._body;
    }
    
    public final void setBody(final Block body) {
        this._body = body;
    }
    
    public final LoopType getLoopType() {
        return this._loopType;
    }
    
    public final void setLoopType(final LoopType loopType) {
        this._loopType = loopType;
    }
    
    @Override
    public final List<Node> getChildren() {
        if (this._condition == null) {
            if (this._body == null) {
                return Collections.emptyList();
            }
            return Collections.singletonList(this._body);
        }
        else {
            if (this._body == null) {
                return Collections.singletonList(this._condition);
            }
            return ArrayUtilities.asUnmodifiableList(this._condition, this._body);
        }
    }
    
    @Override
    public final void writeTo(final ITextOutput output) {
        if (this._condition != null) {
            if (this._loopType == LoopType.PostCondition) {
                output.writeKeyword("do");
            }
            else {
                output.writeKeyword("while");
                output.write(" (");
                this._condition.writeTo(output);
                output.write(')');
            }
        }
        else {
            output.writeKeyword("loop");
        }
        output.writeLine(" {");
        output.indent();
        if (this._body != null) {
            this._body.writeTo(output);
        }
        output.unindent();
        if (this._condition != null && this._loopType == LoopType.PostCondition) {
            output.write("} ");
            output.writeKeyword("while");
            output.write(" (");
            this._condition.writeTo(output);
            output.writeLine(")");
        }
        else {
            output.writeLine("}");
        }
    }
}
