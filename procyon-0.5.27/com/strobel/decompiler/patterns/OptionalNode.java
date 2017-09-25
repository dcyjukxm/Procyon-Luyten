package com.strobel.decompiler.patterns;

import com.strobel.core.*;

public final class OptionalNode extends Pattern
{
    private final INode _node;
    
    public OptionalNode(final INode node) {
        super();
        this._node = VerifyArgument.notNull(node, "node");
    }
    
    public final INode getNode() {
        return this._node;
    }
    
    @Override
    public final boolean matchesCollection(final Role role, final INode position, final Match match, final BacktrackingInfo backtrackingInfo) {
        backtrackingInfo.stack.push(new PossibleMatch(position, match.getCheckPoint()));
        return this._node.matches(position, match);
    }
    
    @Override
    public final boolean matches(final INode other, final Match match) {
        return other == null || other.isNull() || this._node.matches(other, match);
    }
}
