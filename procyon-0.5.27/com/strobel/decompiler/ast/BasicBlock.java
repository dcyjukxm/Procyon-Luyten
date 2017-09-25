package com.strobel.decompiler.ast;

import com.strobel.assembler.*;
import java.util.*;
import com.strobel.decompiler.*;

public final class BasicBlock extends Node
{
    private final Collection<Node> _body;
    
    public BasicBlock() {
        super();
        this._body = new Collection<Node>();
    }
    
    public final List<Node> getBody() {
        return this._body;
    }
    
    @Override
    public final List<Node> getChildren() {
        final ArrayList<Node> childrenCopy = new ArrayList<Node>(this._body.size());
        childrenCopy.addAll(this._body);
        return childrenCopy;
    }
    
    @Override
    public final void writeTo(final ITextOutput output) {
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
