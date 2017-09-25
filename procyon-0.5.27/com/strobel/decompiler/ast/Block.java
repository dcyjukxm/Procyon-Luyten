package com.strobel.decompiler.ast;

import com.strobel.assembler.*;
import com.strobel.core.*;
import java.util.*;
import com.strobel.decompiler.*;

public class Block extends Node
{
    private final Collection<Node> _body;
    private Expression _entryGoto;
    
    public Block() {
        super();
        this._body = new Collection<Node>();
    }
    
    public Block(final Iterable<Node> body) {
        this();
        for (final Node node : VerifyArgument.notNull(body, "body")) {
            this._body.add(node);
        }
    }
    
    public Block(final Node... body) {
        this();
        Collections.addAll(this._body, (Node[])VerifyArgument.notNull((T[])body, "body"));
    }
    
    public final Expression getEntryGoto() {
        return this._entryGoto;
    }
    
    public final void setEntryGoto(final Expression entryGoto) {
        this._entryGoto = entryGoto;
    }
    
    public final List<Node> getBody() {
        return this._body;
    }
    
    @Override
    public final List<Node> getChildren() {
        final ArrayList<Node> childrenCopy = new ArrayList<Node>(this._body.size() + 1);
        if (this._entryGoto != null) {
            childrenCopy.add(this._entryGoto);
        }
        childrenCopy.addAll(this._body);
        return childrenCopy;
    }
    
    @Override
    public void writeTo(final ITextOutput output) {
        final List<Node> children = this.getChildren();
        boolean previousWasSimpleNode = true;
        for (int i = 0, childrenSize = children.size(); i < childrenSize; ++i) {
            final Node child = children.get(i);
            final boolean isSimpleNode = child instanceof Expression || child instanceof Label;
            if ((i != 0 && !isSimpleNode) || !previousWasSimpleNode) {
                output.writeLine();
            }
            child.writeTo(output);
            if (isSimpleNode) {
                output.writeLine();
            }
            previousWasSimpleNode = isSimpleNode;
        }
    }
}
