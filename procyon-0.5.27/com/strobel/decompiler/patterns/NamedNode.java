package com.strobel.decompiler.patterns;

import com.strobel.core.*;

public final class NamedNode extends Pattern
{
    private final String _groupName;
    private final INode _node;
    
    public NamedNode(final String groupName, final INode node) {
        super();
        this._groupName = groupName;
        this._node = VerifyArgument.notNull(node, "node");
    }
    
    public final String getGroupName() {
        return this._groupName;
    }
    
    public final INode getNode() {
        return this._node;
    }
    
    @Override
    public final boolean matches(final INode other, final Match match) {
        match.add(this._groupName, other);
        return this._node.matches(other, match);
    }
}
