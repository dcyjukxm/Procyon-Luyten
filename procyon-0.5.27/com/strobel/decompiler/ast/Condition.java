package com.strobel.decompiler.ast;

import java.util.*;
import com.strobel.core.*;
import com.strobel.decompiler.*;

public final class Condition extends Node
{
    private Expression _condition;
    private Block _trueBlock;
    private Block _falseBlock;
    
    public final Expression getCondition() {
        return this._condition;
    }
    
    public final void setCondition(final Expression condition) {
        this._condition = condition;
    }
    
    public final Block getTrueBlock() {
        return this._trueBlock;
    }
    
    public final void setTrueBlock(final Block trueBlock) {
        this._trueBlock = trueBlock;
    }
    
    public final Block getFalseBlock() {
        return this._falseBlock;
    }
    
    public final void setFalseBlock(final Block falseBlock) {
        this._falseBlock = falseBlock;
    }
    
    @Override
    public final List<Node> getChildren() {
        final int size = ((this._condition != null) ? 1 : 0) + ((this._trueBlock != null) ? 1 : 0) + ((this._falseBlock != null) ? 1 : 0);
        final Node[] children = new Node[size];
        int i = 0;
        if (this._condition != null) {
            children[i++] = this._condition;
        }
        if (this._trueBlock != null) {
            children[i++] = this._trueBlock;
        }
        if (this._falseBlock != null) {
            children[i++] = this._falseBlock;
        }
        return ArrayUtilities.asUnmodifiableList(children);
    }
    
    @Override
    public final void writeTo(final ITextOutput output) {
        output.writeKeyword("if");
        output.write(" (");
        if (this._condition != null) {
            this._condition.writeTo(output);
        }
        else {
            output.write("...");
        }
        output.writeLine(") {");
        output.indent();
        if (this._trueBlock != null) {
            this._trueBlock.writeTo(output);
        }
        output.unindent();
        output.writeLine("}");
        if (this._falseBlock != null && !this._falseBlock.getBody().isEmpty()) {
            output.writeKeyword("else");
            output.writeLine(" {");
            output.indent();
            this._falseBlock.writeTo(output);
            output.unindent();
            output.writeLine("}");
        }
    }
}
