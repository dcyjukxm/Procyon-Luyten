package com.strobel.decompiler.ast;

import com.strobel.assembler.*;
import com.strobel.core.*;
import java.util.*;
import com.strobel.decompiler.*;

public final class TryCatchBlock extends Node
{
    private final List<CatchBlock> _catchBlocks;
    private Block _tryBlock;
    private Block _finallyBlock;
    private boolean _synchronized;
    
    public TryCatchBlock() {
        super();
        this._catchBlocks = new Collection<CatchBlock>();
    }
    
    public final Block getTryBlock() {
        return this._tryBlock;
    }
    
    public final void setTryBlock(final Block tryBlock) {
        this._tryBlock = tryBlock;
    }
    
    public final List<CatchBlock> getCatchBlocks() {
        return this._catchBlocks;
    }
    
    public final Block getFinallyBlock() {
        return this._finallyBlock;
    }
    
    public final void setFinallyBlock(final Block finallyBlock) {
        this._finallyBlock = finallyBlock;
    }
    
    public final boolean isSynchronized() {
        return this._synchronized;
    }
    
    public final void setSynchronized(final boolean simpleSynchronized) {
        this._synchronized = simpleSynchronized;
    }
    
    @Override
    public final List<Node> getChildren() {
        final int size = this._catchBlocks.size() + ((this._tryBlock != null) ? 1 : 0) + ((this._finallyBlock != null) ? 1 : 0);
        final Node[] children = new Node[size];
        int i = 0;
        if (this._tryBlock != null) {
            children[i++] = this._tryBlock;
        }
        for (final CatchBlock catchBlock : this._catchBlocks) {
            children[i++] = catchBlock;
        }
        if (this._finallyBlock != null) {
            children[i++] = this._finallyBlock;
        }
        return ArrayUtilities.asUnmodifiableList(children);
    }
    
    @Override
    public final void writeTo(final ITextOutput output) {
        output.writeKeyword("try");
        output.writeLine(" {");
        output.indent();
        if (this._tryBlock != null) {
            this._tryBlock.writeTo(output);
        }
        output.unindent();
        output.writeLine("}");
        for (final CatchBlock catchBlock : this._catchBlocks) {
            catchBlock.writeTo(output);
        }
        if (this._finallyBlock != null) {
            output.writeKeyword("finally");
            output.writeLine(" {");
            output.indent();
            this._finallyBlock.writeTo(output);
            output.unindent();
            output.writeLine("}");
        }
    }
}
